package org.openjava.probe.client.session;

import org.openjava.probe.shared.OutputStream;
import org.openjava.probe.shared.message.Message;

public interface Session extends OutputStream<Message> {
    long id();

    void send(String command);

    boolean compareAndSet(SessionState expectedState, SessionState newState);

    SessionState setState(SessionState state);

    void destroy();
}
