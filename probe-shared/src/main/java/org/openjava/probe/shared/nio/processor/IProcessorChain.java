package org.openjava.probe.shared.nio.processor;

import org.openjava.probe.shared.nio.session.ISessionDataListener;
import org.openjava.probe.shared.nio.session.ISessionEventListener;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public interface IProcessorChain {
    void registerServer(ServerSocketChannel serverSocket, ISessionEventListener eventListener, ISessionDataListener dataListener);

    void registerConnection(SocketChannel channel, ISessionEventListener eventListener, ISessionDataListener dataListner, long timeoutInMillis);

    void registerSession(SocketChannel channel, ISessionEventListener eventListener, ISessionDataListener dataListener);

}
