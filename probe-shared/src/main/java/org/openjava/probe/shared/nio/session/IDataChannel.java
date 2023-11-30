package org.openjava.probe.shared.nio.session;

import java.io.IOException;

public interface IDataChannel {
    byte[] read() throws IOException;

    void send(byte[] packet);

    void write() throws IOException;

    void registerListeners(ISessionDataListener... listeners);
}
