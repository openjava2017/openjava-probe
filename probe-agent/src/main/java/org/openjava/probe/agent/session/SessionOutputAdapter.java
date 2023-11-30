package org.openjava.probe.agent.session;

import org.openjava.probe.shared.OutputStream;
import org.openjava.probe.shared.nio.session.INioSession;

import java.nio.charset.StandardCharsets;

public class SessionOutputAdapter implements OutputStream<String> {
    protected final INioSession session;

    public SessionOutputAdapter(INioSession session) {
        this.session = session;
    }

    @Override
    public void write(String message) {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        session.send(data);
    }

    @Override
    public void close() {
        session.destroy();
    }
}
