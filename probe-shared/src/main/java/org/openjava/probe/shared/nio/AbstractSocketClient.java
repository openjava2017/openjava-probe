package org.openjava.probe.shared.nio;

import org.openjava.probe.shared.LifeCycle;
import org.openjava.probe.shared.log.Logger;
import org.openjava.probe.shared.log.LoggerFactory;
import org.openjava.probe.shared.nio.exception.OpenSessionException;
import org.openjava.probe.shared.nio.processor.*;
import org.openjava.probe.shared.nio.session.INioSession;
import org.openjava.probe.shared.nio.session.ISessionDataListener;
import org.openjava.probe.shared.nio.session.ISessionEventListener;
import org.openjava.probe.shared.util.AssertUtils;
import org.openjava.probe.shared.util.ScheduledExecutor;
import org.openjava.probe.shared.util.Scheduler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractSocketClient extends LifeCycle {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSocketClient.class);

    // for client side, only one processor is enough
    private final IProcessor<INioSession>[] processors = new IProcessor[1];
    private final IProcessorChain processorChain = new SimpleProcessorChain(this.processors);
    private final ExecutorService executor;
    private final Scheduler scheduler;

    public AbstractSocketClient(ExecutorService executor) {
        this.executor = executor;
        this.scheduler = new ScheduledExecutor("connect-timeout-scanner", true);
    }

    protected INioSession getSession(String host, int port, int connTimeOutInMillis, ISessionDataListener dataListener) throws IOException {
        checkState();
        NioConnectFactory sessionFactory = new NioConnectFactory(host, port, connTimeOutInMillis);
        INioSession session = sessionFactory.createSession(dataListener);
        if (session == null) {
            throw new OpenSessionException("Failed to create nio session");
        }
        return session;
    }

    @Override
    protected void doStart() throws Exception {
        this.processors[0] = new NioSessionProcessor(0, processorChain, executor, scheduler);
        this.processors[0].start();
        LOG.info("Client socket processor manager started");
    }

    @Override
    protected void doStop() throws Exception {
        this.processors[0].stop();
        LOG.info("Client socket processor manager stopped");

        scheduler.shutdown();
    }

    protected abstract void onSessionClosed(INioSession session);

    private void checkState() {
        if (!isRunning()) {
            throw new IllegalStateException("Invalid processor state, state:" + getState());
        }
    }

    private class NioConnectFactory implements ISessionEventListener {
        private final String host;
        private final int port;
        private final int connTimeOutInMillis;
        private volatile INioSession session;
        private final ReentrantLock lock = new ReentrantLock();

        /** Condition for waiting takes */
        private final Condition hasSession = lock.newCondition();

        public NioConnectFactory(String host, int port, int connTimeOutInMillis) {
            AssertUtils.notEmpty(host, "host cannot be null");
            AssertUtils.isTrue(port > 1024, "Invalid port value");
            AssertUtils.isTrue(connTimeOutInMillis > 0, "invalid connTimeOutInMillis value");
            AssertUtils.notNull(executor, "executor cannot be null");

            this.host = host;
            this.port = port;
            this.connTimeOutInMillis = connTimeOutInMillis;
        }

        @Override
        public void onSessionCreated(INioSession session) {
            final ReentrantLock lock = this.lock;
            try {
                lock.lockInterruptibly();

                try {
                    this.session = session;
                    this.hasSession.signalAll();
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException iex) {
                LOG.error("onSessionCreated thread interrupted");
            }
        }

        public INioSession createSession(ISessionDataListener dataListener) throws IOException {
            InetSocketAddress remoteAddress = new InetSocketAddress(host, port);
            boolean result = false;
            SocketChannel channel = null;

            try {
                channel = SocketChannel.open();
                channel.configureBlocking(false);
                channel.connect(remoteAddress);
                processorChain.registerConnection(channel, this, dataListener, connTimeOutInMillis);
                result = true;
            } finally {
                if (!result) {
                    ProcessorUtils.closeQuietly(channel);
                }
            }

            final ReentrantLock lock = this.lock;
            try {
                lock.lockInterruptibly();
                try {
                    if (this.session == null) {
                        this.hasSession.await(connTimeOutInMillis, TimeUnit.MILLISECONDS);
                    }
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException iex) {
                LOG.error("createSession thread interrupted");
            }

            return this.session;
        }

        @Override
        public void onSessionClosed(INioSession session) {
            AbstractSocketClient.this.onSessionClosed(session);
        }

        @Override
        public void onSocketConnectFailed(IOException ex) {
            final ReentrantLock lock = this.lock;
            try {
                lock.lockInterruptibly();

                try {
                    hasSession.signalAll();
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException iex) {
                LOG.error("onSocketConnectFailed thread interrupted");
            }
        }
    }
}
