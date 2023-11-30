package org.openjava.probe.shared.nio.session;

import org.openjava.probe.shared.log.Logger;
import org.openjava.probe.shared.log.LoggerFactory;
import org.openjava.probe.shared.nio.exception.CloseSessionException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class SessionDataChannel implements IDataChannel {
    private static final Logger LOG = LoggerFactory.getLogger(SessionDataChannel.class);

    private static final int PROTOCOL_HEAD_SIZE = 4;

    private INioSession session;

    private final ByteBuffer headerBuffer;

    private ByteBuffer bodyBuffer;

    private final Queue<ByteBuffer> dataBuffer = new ConcurrentLinkedQueue<>();

    private final List<ISessionDataListener> listeners = new CopyOnWriteArrayList<>();

    public SessionDataChannel(INioSession session) {
        this.session = session;
        this.headerBuffer =  ByteBuffer.allocate(PROTOCOL_HEAD_SIZE).order(ByteOrder.LITTLE_ENDIAN);
    }

    @Override
    public byte[] read() throws IOException {
        int numOfByte;
        SocketChannel channel = session.getChannel();

        if (bodyBuffer == null) {
            numOfByte = channel.read(headerBuffer);
            if(numOfByte == -1) {
                throw new CloseSessionException("Nio session[SID=" + session.getId() + "] being closed by remote end");
            }

            if (headerBuffer.hasRemaining()) {
                return null;
            } else {
                headerBuffer.flip();
                int bodySize = headerBuffer.getInt();
                bodyBuffer = ByteBuffer.allocate(bodySize - 4).order(ByteOrder.LITTLE_ENDIAN);
            }
        }

        numOfByte = channel.read(bodyBuffer);
        if(numOfByte == -1) {
            throw new CloseSessionException("Nio session[SID=" + session.getId() + "] being closed by remote end");
        } else {
            LOG.debug("{} bytes read from session[SID={}]", numOfByte, session.getId());
        }

        if (bodyBuffer.hasRemaining()) {
            return null;
        } else {
            bodyBuffer.flip();
            byte[] packet = bodyBuffer.array();
            headerBuffer.clear();
            bodyBuffer = null;

            // fire data received event
            fireDataReceived(packet);
            return packet;
        }
    }

    @Override
    public void send(byte[] packet) {
        if (packet != null) {
            int bodySize = PROTOCOL_HEAD_SIZE + packet.length;
            ByteBuffer data = ByteBuffer.allocate(bodySize).order(ByteOrder.LITTLE_ENDIAN);
            data.putInt(bodySize);
            data.put(packet);
            data.flip();
            dataBuffer.add(data);
            session.getProcessor().registerWriter(session);
        }
    }

    @Override
    public void write() throws IOException {
        try {
            do {
                ByteBuffer request = dataBuffer.peek();
                if (request != null) {
                    int num = session.getChannel().write(request);
                    if(!request.hasRemaining()) {
                        LOG.debug("{} bytes written to session[SID={}]", num, session.getId());
                        dataBuffer.remove();
                    } else {
                        LOG.debug("{} bytes written, {} bytes left[SID={}]", num, request.remaining(), session.getId());
                        break;
                    }
                }
            } while (!dataBuffer.isEmpty());
        } catch (IOException iex) {
            LOG.error("Failed to write to a session[SID={}]", session.getId());
            throw iex;
        } finally {
            if(dataBuffer.isEmpty()) {
                updateKeyWriteInterests(session, false);
            }
        }
    }

    @Override
    public void registerListeners(ISessionDataListener... listeners) {
        if (listeners != null) {
            Collections.addAll(this.listeners, listeners);
        }
    }

    private void fireDataReceived(byte[] packet) {
        for (ISessionDataListener listener : listeners) {
            listener.onDataReceived(session, packet);
        }
    }

    protected void updateKeyWriteInterests(INioSession session, boolean isInterested) {
        SelectionKey key = session.getSelectionKey();

        if (key == null) {
            return;
        }

        int newInterestOps = key.interestOps();

        if (isInterested) {
            newInterestOps |= SelectionKey.OP_WRITE;
        } else {
            newInterestOps &= ~SelectionKey.OP_WRITE;
        }

        key.interestOps(newInterestOps);
    }
}
