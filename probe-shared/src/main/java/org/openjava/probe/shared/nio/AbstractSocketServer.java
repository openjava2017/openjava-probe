package org.openjava.probe.shared.nio;

import org.openjava.probe.shared.LifeCycle;
import org.openjava.probe.shared.log.Logger;
import org.openjava.probe.shared.log.LoggerFactory;
import org.openjava.probe.shared.nio.exception.MultiException;
import org.openjava.probe.shared.nio.processor.*;
import org.openjava.probe.shared.nio.session.INioSession;
import org.openjava.probe.shared.nio.session.ISessionDataListener;
import org.openjava.probe.shared.nio.session.ISessionEventListener;
import org.openjava.probe.shared.util.AssertUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractSocketServer extends LifeCycle implements ISessionEventListener, ISessionDataListener {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSocketServer.class);
    private static final int DEFAULT_SERVER_BACKLOG = 50;

    private final String host;
    private final int port;
    private final int backlog;

    private final IProcessor<INioSession>[] processors;
    private final IProcessorChain processorChain;
    private final ExecutorService executor;

    public AbstractSocketServer(String host, int port) {
        this(host, port, DEFAULT_SERVER_BACKLOG, Runtime.getRuntime().availableProcessors(), Executors.newCachedThreadPool());
    }

    public AbstractSocketServer(String host, int port, int backlog, int processorThreads, ExecutorService executor) {
        AssertUtils.notEmpty(host, "host cannot be empty");
        AssertUtils.isTrue(port > 1024, "invalid port value");
        AssertUtils.isTrue(backlog > 0, "invalid backlog value");
        AssertUtils.isTrue(processorThreads > 0, "invalid processorThreads value");
        AssertUtils.notNull(executor, "executor cannot be null");

        this.host = host;
        this.port = port;
        this.backlog = backlog;
        this.processors = new IProcessor[processorThreads];
        this.processorChain = new SimpleProcessorChain(this.processors);
        this.executor = executor;
    }

    @Override
    public void onSessionCreated(INioSession session) {
    }

    @Override
    public void onSessionClosed(INioSession session) {
    }

    @Override
    public void onSocketConnectFailed(IOException ex) {
        // Never happened
    }

    @Override
    public abstract void onDataReceived(final INioSession session, final byte[] packet);

    @Override
    protected void doStart() throws Exception {
        for (int i = 0; i < processors.length; i++) {
            boolean result = false;
            try {
                processors[i] = new NioSessionProcessor(i, processorChain, executor);
                processors[i].start();
                result = true;
            } finally {
                if (!result) {
                    processors[i].stop();
                }
            }
        }
        LOG.info("Socket processor manager started, pool size=" + processors.length);

        InetSocketAddress address = new InetSocketAddress(host, port);
        ServerSocketChannel socketChannel = ServerSocketChannel.open();
        boolean result = false;
        try {
            socketChannel.configureBlocking(false);
            ServerSocket serverSocket = socketChannel.socket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(address, backlog);
            processorChain.registerServer(socketChannel, this, this);
            result = true;
        } finally {
            if (!result) {
                ProcessorUtils.closeQuietly(socketChannel);
            }
        }
    }

    @Override
    protected void doStop() throws Exception {
        MultiException exception = new MultiException();
        for (int i = 0; i < processors.length; i++) {
            try {
                processors[i].stop();
            } catch (Exception ex) {
                exception.add(ex);
            }
        }
        exception.ifExceptionThrow();
        LOG.info("Socket processor manager stopped");
    }
}