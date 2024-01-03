package org.openjava.probe.agent.server;

import org.openjava.probe.agent.advice.MethodAdviceManager;
import org.openjava.probe.agent.api.ThreadLocalMethodListener;
import org.openjava.probe.agent.context.Context;
import org.openjava.probe.agent.context.ExecuteContext;
import org.openjava.probe.agent.context.Environment;
import org.openjava.probe.agent.context.ProbeEnvironment;
import org.openjava.probe.agent.handler.UserMessageHandler;
import org.openjava.probe.agent.session.Session;
import org.openjava.probe.agent.session.SessionState;
import org.openjava.probe.agent.session.UserSession;
import org.openjava.probe.agent.transformer.ClassTransformerManager;
import org.openjava.probe.core.api.ProbeMethodAPI;
import org.openjava.probe.shared.ErrorCode;
import org.openjava.probe.shared.LifeCycle;
import org.openjava.probe.shared.exception.ProbeServiceException;
import org.openjava.probe.shared.log.Logger;
import org.openjava.probe.shared.log.LoggerFactory;
import org.openjava.probe.shared.message.Message;
import org.openjava.probe.shared.nio.AbstractSocketServer;
import org.openjava.probe.shared.nio.session.INioSession;
import org.openjava.probe.shared.util.ProbeThreadFactory;

import java.lang.instrument.Instrumentation;
import java.util.Map;
import java.util.concurrent.*;

public class ProbeAgentServer extends LifeCycle {
    private static final Logger LOG = LoggerFactory.getLogger(ProbeAgentServer.class);

    private static ProbeAgentServer agentServer;
    private final Environment environment;
    private final Instrumentation instrumentation;
    private final ClassTransformerManager transformerManager;
    private final ExecutorService executorService;
    private final UserMessageServer messageServer;

    private ProbeAgentServer(Environment environment, Instrumentation instrumentation) {
        this.environment = environment;
        this.instrumentation = instrumentation;
        this.transformerManager = new ClassTransformerManager();
        // two threads needed at least, one for nio processor and another for handler executor
        this.executorService = Executors.newFixedThreadPool(2, ProbeThreadFactory.getInstance());

        String host = this.environment.getRequiredProperty("probe.server.host");
        int port = this.environment.getRequiredProperty("probe.server.port", Integer.class);
        this.messageServer = new UserMessageServer(host, port);
    }

    public static synchronized ProbeAgentServer getInstance(String args, Instrumentation instrumentation) {
        if (agentServer == null) {
            agentServer = new ProbeAgentServer(new ProbeEnvironment(args), instrumentation);
        }
        return agentServer;
    }

    public static synchronized ProbeAgentServer getInstance() {
        if (agentServer == null || !agentServer.isStarted()) {
            throw new ProbeServiceException(ErrorCode.ILLEGAL_STATE_ERROR, "Agent Server not inited");
        }
        return agentServer;
    }

    public ClassTransformerManager transformerManager() {
        return this.transformerManager;
    }

    protected void doStart() throws Exception {
        this.instrumentation.addTransformer(transformerManager, true);
        this.messageServer.start();
        ProbeMethodAPI.installMethodListener(new ThreadLocalMethodListener());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                ProbeAgentServer.this.stop();
            } catch (Exception ex) {
                // Ignore it
            }
        }));
    }

    protected void doStop() throws Exception {
        ProbeMethodAPI.reset();
        this.messageServer.stop();
        this.instrumentation.removeTransformer(transformerManager);
        this.transformerManager.clearClassFileTransformers();
        MethodAdviceManager.getInstance().clearAllMethodAdvices();
    }

    private class UserMessageServer extends AbstractSocketServer {
        private final BlockingQueue<UserMessageHandler> handlers = new LinkedBlockingQueue<>();
        private final Map<Long, Session> sessions = new ConcurrentHashMap<>();

        public UserMessageServer(String host, int port) {
            super(host, port, 10, 1, executorService);
        }

        @Override
        public void onSessionCreated(INioSession session) {
            sessions.put(session.getId(), new UserSession(session));
        }

        @Override
        public void onSessionClosed(INioSession nioSession) {
            Session session = sessions.remove(nioSession.getId());
            if (session != null) {
                session.setState(SessionState.CLOSED);
            }
        }

        @Override
        public void onDataReceived(final INioSession nioSession, final byte[] packet) {
            Session session = sessions.get(nioSession.getId());
            if (session != null) {
                Message message = Message.from(packet);
                Context context = ExecuteContext.of(environment, instrumentation, session);
                handlers.add(new UserMessageHandler(context, message));
            } else {
                LOG.error("User session not found: {}", nioSession.getId());
            }
        }

        protected void doStart() throws Exception {
            super.doStart();
            // all the commands are executed in one independent thread for thread-safe reason
            ProbeAgentServer.this.executorService.execute(() -> {
                LOG.info("User message handle thread started");
                while (isRunning()) {
                    try {
                        UserMessageHandler handler = handlers.poll(5, TimeUnit.SECONDS);
                        if (handler != null) {
                            handler.handle();
                        }
                    } catch (Exception ex) {
                        if (Thread.interrupted()) {
                            break;
                        }
                        ex.printStackTrace();
                        //TODO: remove printStackTrace
                        LOG.error("User message handle exception", ex);
                    }
                }
                LOG.info("User message handle thread terminated");
            });
        }

        protected void doStop() throws Exception {
            for (Map.Entry<Long, Session> entry : sessions.entrySet()) {
                entry.getValue().destroy();
            }
            sessions.clear();
            super.doStop();
        }
    }
}
