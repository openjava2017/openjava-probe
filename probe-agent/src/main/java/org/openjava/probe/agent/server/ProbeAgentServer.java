package org.openjava.probe.agent.server;

import org.openjava.probe.agent.advice.MethodAdviceManager;
import org.openjava.probe.agent.api.ThreadLocalMethodListener;
import org.openjava.probe.agent.command.CommandExecutor;
import org.openjava.probe.agent.command.CommandWrapper;
import org.openjava.probe.agent.command.Context;
import org.openjava.probe.agent.command.ExecuteContext;
import org.openjava.probe.agent.env.Environment;
import org.openjava.probe.agent.env.ProbeEnvironment;
import org.openjava.probe.agent.session.Session;
import org.openjava.probe.agent.session.UserSession;
import org.openjava.probe.agent.transformer.ClassTransformerManager;
import org.openjava.probe.core.api.ProbeMethodAPI;
import org.openjava.probe.shared.ErrorCode;
import org.openjava.probe.shared.LifeCycle;
import org.openjava.probe.shared.exception.ProbeServiceException;
import org.openjava.probe.shared.log.Logger;
import org.openjava.probe.shared.log.LoggerFactory;
import org.openjava.probe.shared.message.Message;
import org.openjava.probe.shared.message.MessageHeader;
import org.openjava.probe.shared.message.PayloadHelper;
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
    private final CommandExecutor commandExecutor;

    private ProbeAgentServer(Environment environment, Instrumentation instrumentation) {
        this.environment = environment;
        this.instrumentation = instrumentation;
        this.transformerManager = new ClassTransformerManager();
        // two threads needed at least, one for nio processor and another for command executor
        this.executorService = Executors.newFixedThreadPool(2, ProbeThreadFactory.getInstance());

        String host = this.environment.getRequiredProperty("probe.server.host");
        int port = this.environment.getRequiredProperty("probe.server.port", Integer.class);
        this.commandExecutor = new SocketServerImpl(host, port);
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
        this.commandExecutor.start();
        ProbeMethodAPI.installMethodListener(new ThreadLocalMethodListener());

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    ProbeAgentServer.this.stop();
                } catch (Exception ex) {
                    // Ignore it
                }
            }
        });
    }

    protected void doStop() throws Exception {
        ProbeMethodAPI.reset();
        this.commandExecutor.stop();
        this.instrumentation.removeTransformer(transformerManager);
        this.transformerManager.clearClassFileTransformers();
        MethodAdviceManager.getInstance().clearAllMethodAdvices();
    }

    private class SocketServerImpl extends AbstractSocketServer implements CommandExecutor {
        private final BlockingQueue<CommandWrapper> commands = new LinkedBlockingQueue<>();
        private final Map<Long, Session> sessions = new ConcurrentHashMap<>();

        public SocketServerImpl(String host, int port) {
            super(host, port, 10, 1, executorService);
        }

        public void submit(CommandWrapper command) {
            commands.add(command);
        }

        @Override
        public void onSessionCreated(INioSession session) {
            sessions.put(session.getId(), new UserSession(session));
        }

        @Override
        public void onSessionClosed(INioSession session) {
            Session userSession = sessions.remove(session.getId());
            if (userSession != null) {
                userSession.destroy();
            }
        }

        @Override
        public void onDataReceived(final INioSession nioSession, final byte[] packet) {
            Session session = sessions.get(nioSession.getId());
            if (session != null) {
                Message message = Message.from(packet);
                if (MessageHeader.USER_COMMAND.equalTo(message.header())) {
                    Context context = ExecuteContext.of(environment, instrumentation, session);
                    CommandWrapper command = CommandWrapper.of(context, message.payload(PayloadHelper.STRING_DECODER));
                    submit(command);
                } else {
                    LOG.error("Ignore the command from the client: suppose it never happened");
                }
            } else {
                LOG.error("User session not found: {}", nioSession.getId());
            }
        }

        protected void doStart() throws Exception {
            super.doStart();
            // all the commands are executed in one independent thread for thread-safe reason
            ProbeAgentServer.this.executorService.execute(() -> {
                LOG.info("Probe command execute thread started");
                while (isRunning()) {
                    try {
                        CommandWrapper command = commands.poll(5, TimeUnit.SECONDS);
                        if (command != null) {
                            command.execute();
                        }
                    } catch (Exception ex) {
                        if (Thread.interrupted()) {
                            break;
                        }
                        ex.printStackTrace();
                        //TODO: remove printStackTrace
                        LOG.error("Probe command execute exception", ex);
                    }
                }
                LOG.info("Probe command execute thread terminated");
            });
        }

        protected void doStop() throws Exception {
            super.doStop();
            for (Map.Entry<Long, Session> entry : sessions.entrySet()) {
                entry.getValue().destroy();
            }
            sessions.clear();
        }
    }
}
