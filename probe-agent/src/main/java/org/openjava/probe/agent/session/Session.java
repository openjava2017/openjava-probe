package org.openjava.probe.agent.session;

import org.openjava.probe.agent.advice.ProbeMethodAdvice;
import org.openjava.probe.shared.OutputStream;
import org.openjava.probe.shared.message.Message;

public interface Session extends OutputStream<Message> {
    long id();

    void addMethodAdvice(ProbeMethodAdvice advice);

    void clearMethodAdvices();

    SessionState getState();

    boolean compareAndSet(SessionState expectedState, SessionState newState);

    SessionState setState(SessionState state);

    void synchronize();

    void destroy();
}
