package org.openjava.probe.shared.nio.session;

public interface ISessionDataListener {
    void onDataReceived(INioSession session, byte[] packet);
}
