package org.openjava.probe.shared.nio.session;

import org.openjava.probe.shared.nio.processor.IProcessor;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public interface INioSession {
    long getId();

    SelectionKey getSelectionKey();
    
    SocketChannel getChannel();
    
    IProcessor<INioSession> getProcessor();

    IDataChannel getDataChannel();

    void send(byte[] packet);

    SessionState getState();
    
    void destroy();
}
