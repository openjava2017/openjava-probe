package org.openjava.probe.agent.session;

import org.openjava.probe.shared.OutputStream;
import org.openjava.probe.shared.message.Message;
import org.openjava.probe.shared.nio.session.INioSession;
import org.openjava.probe.shared.nio.session.SessionState;

public class SessionOutputAdapter implements OutputStream<Message> {
    protected final INioSession session;

    public SessionOutputAdapter(INioSession session) {
        this.session = session;
    }

    @Override
    public void write(Message message) {
        if (session.getState() == SessionState.CONNECTED) {
            session.send(message.toBytes());
        }
    }

    @Override
    public void close() {
        session.destroy();
    }
}
