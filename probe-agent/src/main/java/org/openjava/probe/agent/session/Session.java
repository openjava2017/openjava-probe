package org.openjava.probe.agent.session;

import org.openjava.probe.agent.advice.ProbeMethodAdvice;
import org.openjava.probe.shared.OutputStream;

public interface Session extends OutputStream<String> {
    long id();

    void addMethodAdvice(ProbeMethodAdvice advice);

    void clearMethodAdvices();

    boolean compareAndSet(SessionState expectedState, SessionState newState);

    SessionState setState(SessionState state);

    void destroy();
}
