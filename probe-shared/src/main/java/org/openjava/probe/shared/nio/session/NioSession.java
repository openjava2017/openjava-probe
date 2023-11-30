package org.openjava.probe.shared.nio.session;

import org.openjava.probe.shared.nio.processor.IProcessor;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class NioSession implements INioSession {
    private static final AtomicLong SESSION_ID = new AtomicLong(0);

    private final long sessionId;
    
    private final SelectionKey key;

    private final SocketChannel channel;

    private final IProcessor<INioSession> processor;

    private final IDataChannel dataChannel;

    protected final AtomicReference<SessionState> state;

    protected NioSession(SocketChannel channel, SelectionKey key, IProcessor<INioSession> processor) {
        this.sessionId = SESSION_ID.incrementAndGet();
        this.channel = channel;
        this.key = key;
        this.processor = processor;
        this.dataChannel =  new SessionDataChannel(this);
//        this.dataChannel =  new BufferedDataChannel(this);
        this.state = new AtomicReference<>(SessionState.CONNECTED);
    }
    
    @Override
    public long getId() {
        return this.sessionId;
    }

    @Override
    public SocketChannel getChannel() {
        return this.channel;
    }
    
    @Override
    public SelectionKey getSelectionKey() {
        return this.key;
    }

    @Override
    public IProcessor<INioSession> getProcessor() {
        return this.processor;
    }

    @Override
    public IDataChannel getDataChannel() {
        checkState();
        return this.dataChannel;
    }

    @Override
    public void send(byte[] packet) {
        getDataChannel().send(packet);
    }

    @Override
    public SessionState getState()
    {
        return this.state.get();
    }
    
    @Override
    public void destroy() {
        if (state.compareAndSet(SessionState.CONNECTED, SessionState.CLOSING)) {
            getProcessor().unregisterSession(this);
        }
    }

    private void checkState() {
        if (getState() != SessionState.CONNECTED) {
            throw new IllegalStateException("Invalid session state, state:" + getState());
        }
    }

    public static NioSession create(SocketChannel channel, SelectionKey key, IProcessor<INioSession> processor) {
        return new NioSession(channel, key, processor);
    }
}