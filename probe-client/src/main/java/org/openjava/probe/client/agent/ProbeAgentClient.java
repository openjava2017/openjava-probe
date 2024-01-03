package org.openjava.probe.client.agent;

import org.openjava.probe.client.context.Context;
import org.openjava.probe.client.context.Environment;
import org.openjava.probe.client.context.UserContext;
import org.openjava.probe.client.gui.event.DetachEvent;
import org.openjava.probe.client.gui.event.GuiEventMulticaster;
import org.openjava.probe.client.handler.MessageHandlerWrapper;
import org.openjava.probe.client.session.Session;
import org.openjava.probe.client.session.SessionState;
import org.openjava.probe.client.session.UserSession;
import org.openjava.probe.shared.log.Logger;
import org.openjava.probe.shared.log.LoggerFactory;
import org.openjava.probe.shared.message.Message;
import org.openjava.probe.shared.nio.AbstractSocketClient;
import org.openjava.probe.shared.nio.session.INioSession;
import org.openjava.probe.shared.nio.session.ISessionDataListener;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

public class ProbeAgentClient extends AbstractSocketClient implements ISessionDataListener {
    private static final Logger LOG = LoggerFactory.getLogger(ProbeAgentClient.class);

    private final Environment environment;
    private final ExecutorService executorService;
    private final BlockingQueue<MessageHandlerWrapper> messages = new LinkedBlockingQueue<>();
    private final Map<Long, Session> sessions = new ConcurrentHashMap<>();

    public ProbeAgentClient(Environment environment, ExecutorService executorService) {
        super(executorService);
        this.environment = environment;
        this.executorService = executorService;
    }

    public Session connect(String host, int port, int timeout) throws IOException {
        INioSession nioSession = getSession(host, port, timeout, this);
        Session session = new UserSession(nioSession);
        sessions.put(nioSession.getId(), session);
        return session;
    }

    @Override
    public void onDataReceived(INioSession nioSession, byte[] packet) {
        Session session = sessions.get(nioSession.getId());
        if (session != null) {
            Message message = Message.from(packet);
            Context context = UserContext.of(environment, session);
            messages.add(new MessageHandlerWrapper(context, message));
        } else {
            LOG.error("User session not found: {}", nioSession.getId());
        }
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        ProbeAgentClient.this.executorService.execute(() -> {
            LOG.info("Agent handle thread started");
            while (isRunning()) {
                try {
                    MessageHandlerWrapper handler = messages.poll(5, TimeUnit.SECONDS);
                    if (handler != null) {
                        handler.handle();
                    }
                } catch (Exception ex) {
                    if (Thread.interrupted()) {
                        break;
                    }
                    LOG.error("Agent handle exception", ex);
                }
            }
            LOG.info("Agent handle thread terminated");
        });
    }

    @Override
    protected void doStop() throws Exception {
        for (Map.Entry<Long, Session> entry : sessions.entrySet()) {
            entry.getValue().destroy();
        }
        super.doStop();
    }

    protected void onSessionClosed(INioSession nioSession) {
        Session session = sessions.remove(nioSession.getId());
        if (session != null) {
            session.setState(SessionState.CLOSED);
            GuiEventMulticaster.getInstance().fireDetachEvent(new DetachEvent(this, session));
        }
    }
}
