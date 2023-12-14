package org.openjava.probe.client.agent;

import org.openjava.probe.client.command.Command;
import org.openjava.probe.client.command.Context;
import org.openjava.probe.client.command.UserCommandFactory;
import org.openjava.probe.client.command.UserContext;
import org.openjava.probe.client.env.Environment;
import org.openjava.probe.client.session.Session;
import org.openjava.probe.client.session.UserSession;
import org.openjava.probe.shared.LifeCycle;
import org.openjava.probe.shared.log.Logger;
import org.openjava.probe.shared.log.LoggerFactory;
import org.openjava.probe.shared.message.Message;
import org.openjava.probe.shared.message.MessageHeader;
import org.openjava.probe.shared.message.PayloadHelper;
import org.openjava.probe.shared.nio.AbstractSocketClient;
import org.openjava.probe.shared.nio.session.INioSession;
import org.openjava.probe.shared.nio.session.ISessionDataListener;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ProbeAgentClient extends LifeCycle implements UserCommandListener {
    private static final Logger LOG = LoggerFactory.getLogger(ProbeAgentClient.class);

    private final Environment environment;
    private final ExecutorService executorService;
    private final SocketClientImpl client;

    public ProbeAgentClient(Environment environment, ExecutorService executorService) {
        this.environment = environment;
        this.executorService = executorService;

        String host = environment.getRequiredProperty("probe.server.host");
        int port = environment.getRequiredProperty("probe.server.port", Integer.class);
        int timeout = environment.getProperty("probe.client.connTimeOut", Integer.class, 4000);
        this.client = new SocketClientImpl(host, port, timeout, executorService);
    }

    public void onCommand(String command) {
        if (command.length() > 0) {
            this.client.send(Message.of(MessageHeader.USER_COMMAND, command, PayloadHelper.STRING_ENCODER));
        }
    }

    public boolean requireIdle() throws InterruptedException {
        return this.client.requireIdle();
    }

    @Override
    protected void doStart() throws Exception {
        this.client.start();
    }

    @Override
    protected void doStop() throws Exception {
        this.client.stop();
    }

    private class SocketClientImpl extends AbstractSocketClient implements ISessionDataListener {
        private Session session;
        private final BlockingQueue<Message> commands = new LinkedBlockingQueue<>();

        public SocketClientImpl(String host, int port, int connTimeOutInMillis, ExecutorService executor) {
            super(host, port, connTimeOutInMillis, executor);
        }

        @Override
        public void onDataReceived(INioSession session, byte[] packet) {
            commands.add(Message.from(packet));
        }

        public void send(Message message) {
            this.session.write(message);
        }

        public boolean requireIdle() throws InterruptedException {
            return this.session.requireIdle();
        }

        @Override
        protected void doStart() throws Exception {
            super.doStart();
            this.session = new UserSession(getSession(this));
            ProbeAgentClient.this.executorService.execute(() -> {
                LOG.info("User command handle thread started");
                while (isRunning()) {
                    try {
                        Message message = commands.poll(5, TimeUnit.SECONDS);
                        if (message != null) {
                            Command userCommand = UserCommandFactory.getInstance().getCommand(message.header(), message.payload());
                            Context context = UserContext.of(environment, session);
                            userCommand.execute(context);
                        }
                    } catch (Exception ex) {
                        if (Thread.interrupted()) {
                            break;
                        }
                        LOG.error("User command handle exception", ex);
                    }
                }
                LOG.info("User command handle thread terminated");
            });
        }

        @Override
        protected void doStop() throws Exception {
            this.session.destroy();
            super.doStop();
        }

        protected void onSessionClosed(INioSession session) {
            try {
                ProbeAgentClient.this.stop();
            } catch (Exception ex) {
                LOG.error("Probe agent client stop exception", ex);
                System.exit(1);
            }
            System.exit(0);
        }
    }
}
