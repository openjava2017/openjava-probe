package org.openjava.probe.shared.nio.processor;

import org.openjava.probe.shared.LifeCycle;
import org.openjava.probe.shared.log.Logger;
import org.openjava.probe.shared.log.LoggerFactory;
import org.openjava.probe.shared.nio.exception.CloseSessionException;
import org.openjava.probe.shared.nio.exception.OpenSessionException;
import org.openjava.probe.shared.nio.session.*;
import org.openjava.probe.shared.util.Scheduler;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class NioSessionProcessor extends LifeCycle implements IProcessor<INioSession> {

    private static final Logger LOG = LoggerFactory.getLogger(NioSessionProcessor.class);

    private final long id;
    private Thread current;
    private Selector selector;
    private final IProcessorChain processorChain;
    private final ExecutorService executor;
    private final Scheduler scheduler;
    
    private final Queue<Runnable> changes = new ConcurrentLinkedQueue<>();
    private final AtomicReference<State> state = new AtomicReference<>(State.PROCESS);

    public NioSessionProcessor(long id, IProcessorChain processorChain, ExecutorService executor) {
        this(id, processorChain, executor, null);
    }
    
    public NioSessionProcessor(long id, IProcessorChain processorChain, ExecutorService executor, Scheduler scheduler) {
        this.id = id;
        this.processorChain = processorChain;
        this.executor = executor;
        this.scheduler = scheduler;
    }
    
    @Override
    public void registerServer(ServerSocketChannel serverSocket, ISessionEventListener eventListener,
                               ISessionDataListener dataListener) {
        checkState();
        submit(this.new Acceptor(serverSocket, eventListener, dataListener));
    }
    
    @Override
    public void registerConnection(SocketChannel channel, ISessionEventListener eventListener,
                                   ISessionDataListener dataListener, long timeoutInMillis) {
        checkState();
        submit(this.new Connect(channel, eventListener, dataListener, timeoutInMillis));
    }
    
    @Override
    public void registerSession(SocketChannel channel, ISessionEventListener eventListener, ISessionDataListener dataListener) {
        checkState();
        submit(this.new Register(channel, eventListener, dataListener));
    }
    
    @Override
    public void registerWriter(INioSession session) {
        checkState();
        submit(this.new Writer(session));
    }

    @Override
    public void unregisterSession(INioSession session) {
        checkState();
        submit(ProcessorUtils.CloseCommand.create(session));
    }
    
    public void submit(Runnable change) {
        // This method may be called from the selector thread, and therefore
        // we could directly run the change without queueing, but this may
        // lead to stack overflows on a busy server, so we always offer the
        // change to the queue and process the state.
        changes.offer(change);

        out: 
        while (true) {
            switch (state.get()) {
                case SELECT:
                    // Avoid multiple wakeup() calls if we the CAS fails
                    if (!state.compareAndSet(State.SELECT, State.WAKEUP)) {
                        continue;
                    }
                    wakeup();
                    break out;
                case CHANGES:
                    // Tell the selector thread that we have more changes.
                    // If we fail to CAS, we possibly need to wakeup(), so loop.
                    if (state.compareAndSet(State.CHANGES, State.MORE_CHANGES)) {
                        break out;
                    }
                    continue;
                case WAKEUP:
                    // Do nothing, we have already a wakeup scheduled
                    break out;
                case MORE_CHANGES:
                    // Do nothing, we already notified the selector thread of more changes
                    break out;
                case PROCESS:
                    // Do nothing, the changes will be run after the processing
                    break out;
                default:
                    throw new IllegalStateException();
            }
        }
    }
    
    @Override
    public long id()
    {
        return id;
    }
    
    private void wakeup()
    {
        selector.wakeup();
    }
    
    protected void doStart() throws Exception {
        boolean result = false;
        // Open a new selector
        try {
            selector = Selector.open();
            state.set(State.PROCESS);
            executor.execute(this.new Processor());
            result = true;
        } finally {
            if (!result) {
                ProcessorUtils.closeQuietly(selector);
            }
        }
    }

    protected void doStop() throws Exception {
        LOG.debug("Stopping socket processor {}", id());
        submit(ProcessorUtils.StopCommand.create(selector));
        LOG.debug("Stopped socket processor {}", id());
    }
    
    private void checkState() {
        if (!isRunning()) {
            throw new IllegalStateException("Invalid processor state, state:" + getState());
        }
    }
    
    private class Processor implements Runnable {
        @Override
        public void run() {
            current = Thread.currentThread();
            String name = current.getName();
            try {
                current.setName("nio-selector-" + NioSessionProcessor.this.id());
                LOG.debug("Starting the processor thread {}", current.getName());
                while (isRunning()) {
                    select();
                }
                runChanges();
            } finally {
                LOG.debug("Stopped the processor thread {}", current.getName());
                current.setName(name);
            }
        }
        
        public void select() {
            try {
                state.set(State.CHANGES);

                // Run the changes, and only exit if we ran all changes
                out: 
                while(true) {
                    switch (state.get()) {
                        case CHANGES:
                            runChanges();
                            if (state.compareAndSet(State.CHANGES, State.SELECT))
                                break out;
                            continue;
                        case MORE_CHANGES:
                            runChanges();
                            state.set(State.CHANGES);
                            continue;
                        default:
                            throw new IllegalStateException();    
                    }
                }
                // Must check first for SELECT and *then* for WAKEUP
                // because we read the state twice in assert, and
                // it could change from SELECT to WAKEUP in between.
                assert state.get() == State.SELECT || state.get() == State.WAKEUP;

                LOG.debug("Selector loop waiting on selector {}", current.getName());
                int keys = selector.select();
                LOG.debug("Selector loop woken up from selector {}, {} selected", current.getName(), keys);

                state.set(State.PROCESS);
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                for (SelectionKey key : selectedKeys) {
                    if (key.isValid()) {
                        processKey(key);
                    } else {
                        LOG.debug("Selector loop ignoring invalid key for channel {}", key.channel());
                        Object attachment = key.attachment();
                        if (attachment instanceof SessionContext) {
                            destroy((SessionContext) attachment);
                        }
                    }
                }
                selectedKeys.clear();
            } catch (Throwable x) {
                if (isRunning()) {
                    LOG.warn("Selector failed", x);
                }
            }
        }
        
        private void processKey(SelectionKey key) {
            Object attachment = key.attachment();
            try {
                if (attachment instanceof SessionContext) {
                    processSession((SessionContext) attachment);
                } else if (key.isConnectable()) {
                    processConnect(key);
                } else if (key.isAcceptable()) {
                    processAccept(key);
                } else {
                    throw new IllegalStateException();
                }
            } catch (IOException iex) {
                if (iex instanceof CloseSessionException) {
                    LOG.info(iex.getMessage());
                } else {
                    LOG.error("processKey io exception", iex);
                }

                if (attachment instanceof SessionContext) {
                    destroy((SessionContext) attachment);
                }
            } catch (CancelledKeyException cke) {
                LOG.error("Ignoring cancelled key for channel {}", key.channel());
                if (attachment instanceof SessionContext) {
                    destroy((SessionContext) attachment);
                }
            } catch (Throwable ex) {
                LOG.error("Could not process key for channel " + key.channel(), ex);
            }
        }
        
        private void processSession(SessionContext context) throws IOException {
            INioSession session = context.session();
            SelectionKey key = session.getSelectionKey();
            if (key.isReadable()) {
                LOG.debug("Starting to read the session[SID={}]", session.getId());
                byte[] packet = session.getDataChannel().read();
                while (packet != null) { // Make sure we will have better socket IO usage
                    // Retry until no data in socket
                    packet = session.getDataChannel().read();
                }
            } else if (key.isWritable()) {
                LOG.debug("Starting to write the data, [SID={}]", session.getId());
                session.getDataChannel().write();
            }
        }
        
        private void processAccept(SelectionKey key) {
            SocketChannel channel = null;
            ServerSocketChannel serverSocket = (ServerSocketChannel)key.channel();
            
            Acceptor acceptor = (Acceptor) key.attachment();
            try {
                while ((channel = serverSocket.accept()) != null) {
                    processorChain.registerSession(channel, acceptor.eventListener, acceptor.dataListener);
                }
            } catch (Throwable ex) {
                ProcessorUtils.closeQuietly(channel);
                LOG.error("Accept failed for channel " + channel, ex);
            }
        }
        
        private void processConnect(SelectionKey key) {
            Connect connect = (Connect) key.attachment();
            SocketChannel channel = (SocketChannel)key.channel();
            try {
                boolean connected = channel.finishConnect();
                if (connected) {
                    connect.timeout.cancel();
                    key.interestOps(0);
                    registerSession(channel, connect.eventListener, connect.dataListener);
                } else {
                    throw new OpenSessionException("Socket channel not connected successfully");
                }
            } catch (IOException ex) {
                LOG.error("Socket connect failed", ex);
                connect.failed(ex);
            }
        }
        
        private void destroy(SessionContext context) {
            INioSession session = context.session();
            ProcessorUtils.CloseCommand.create(session).run();
            // fire session removed
            context.fireSessionClosed();
            LOG.info("Closed the session[SID={}]", session.getId());
        }
        
        private void runChanges() {
            Runnable change;
            while ((change = changes.poll()) != null) {
                try {
                    LOG.debug("Running {} change on selector {}", change.getClass().getSimpleName(), current.getName());
                    change.run();
                } catch (Throwable ex) {
                    LOG.debug("Could not run {} change ", change.getClass().getSimpleName(), ex);
                }
            }
        }
    }
    
    private class Acceptor implements Runnable {
        private final ISessionEventListener eventListener;
        private final ISessionDataListener dataListener;
        private final ServerSocketChannel channel;

        public Acceptor(ServerSocketChannel channel, ISessionEventListener eventListener, ISessionDataListener dataListener) {
            this.channel = channel;
            this.eventListener = eventListener;
            this.dataListener = dataListener;
        }

        @Override
        public void run() {
            try {
                LOG.debug("Registering the accept event to NIO selector {}", current.getName());
                channel.register(selector, SelectionKey.OP_ACCEPT, this);
                LOG.info("Server started up and listening on {}", channel.getLocalAddress());
            } catch (Throwable ex) {
                ProcessorUtils.closeQuietly(channel);
                LOG.error("Register the accept event exception", ex);
            }
        }
    }
    
    private class Register implements Runnable {
        private final SocketChannel channel;
        private final ISessionEventListener eventListener;
        private final ISessionDataListener dataListener;
        
        public Register(SocketChannel channel, ISessionEventListener eventListener, ISessionDataListener dataListener) {
            this.channel = channel;
            this.eventListener = eventListener;
            this.dataListener = dataListener;
        }
        
        @Override
        public void run() {
            NioSession session = null;
            try {
                channel.configureBlocking(false);
                SelectionKey key = channel.register(selector, SelectionKey.OP_READ, null);
                session = NioSession.create(channel, key, NioSessionProcessor.this);
                // register data listener for data channel
                session.getDataChannel().registerListeners(dataListener);
                SessionContext context = SessionContext.create(session, eventListener);
                key.attach(context);
                // Register this session to manager
                context.fireSessionCreated();
                LOG.debug("Registered the read event to NIO selector {}", current.getName());
            } catch (IOException iex) {
                ProcessorUtils.CloseCommand.create(session).run();
                LOG.error("Register NIO read event exception", iex);
            } catch (Throwable ex) {
                LOG.error("Register NIO session unknown exception", ex);
            }
        }
    }
    
    private class Writer implements Runnable {
        private final INioSession session;
        
        public Writer(INioSession session) {
            this.session = session;
        }
        
        @Override
        public void run() {
            try {
                SelectionKey key = session.getSelectionKey();
                
                if (key != null && key.isValid()) {
                    int newInterestOps = key.interestOps() | SelectionKey.OP_WRITE;
                    key.interestOps(newInterestOps);
                    LOG.debug("Registered the write event to NIO selector {}", current.getName());
                } else {
                    LOG.warn("Registered write event failed, invalid selection key found");
                }
            } catch (Throwable ex) {
                LOG.error("Register NIO write event exception", ex);
                ProcessorUtils.CloseCommand.create(session).run();
            }
        }
    }
    
    private class Connect implements Runnable {
        private final AtomicBoolean failed = new AtomicBoolean(false);
        private final SocketChannel channel;
        private final ISessionEventListener eventListener;
        private final ISessionDataListener dataListener;
        private final long timeOutInMillis;
        private Scheduler.Task timeout;
        
        public Connect(SocketChannel channel, ISessionEventListener eventListener,
            ISessionDataListener dataListener, long timeOutInMillis) {
            this.channel = channel;
            this.eventListener = eventListener;
            this.dataListener = dataListener;
            this.timeOutInMillis = timeOutInMillis;
        }
        
        @Override
        public void run() {
            try {
                timeout = scheduler.schedule(new ConnectTimeout(this), timeOutInMillis, TimeUnit.MILLISECONDS);
                channel.register(selector, SelectionKey.OP_CONNECT, this);
                LOG.debug("Registered the connect event to NIO selector {}", current.getName());
            } catch (IOException ex) {
                LOG.error("Register the connect event exception", ex);
                failed(ex);
            }
        }
        
        protected void failed(IOException failure) {
            if (failed.compareAndSet(false, true)) {
                ProcessorUtils.closeQuietly(channel);
                timeout.cancel();
                eventListener.onSocketConnectFailed(failure);
            }
        }
    }
    
    private class ConnectTimeout implements Runnable {
        private final Connect connect;

        private ConnectTimeout(Connect connect) {
            this.connect = connect;
        }

        @Override
        public void run() {
            SocketChannel channel = connect.channel;
            if (channel.isConnectionPending()) {
                LOG.error("Connect timed out while connecting, closing it");
                connect.failed(new OpenSessionException("Socket connect time out"));
            }
        }
    }
    
    private enum State {
        CHANGES, MORE_CHANGES, SELECT, WAKEUP, PROCESS
    }
}
