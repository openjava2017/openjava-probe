package org.openjava.probe.agent.session;

import org.openjava.probe.agent.advice.ProbeMethodAdvice;
import org.openjava.probe.shared.OutputStream;
import org.openjava.probe.shared.message.Message;

import java.util.Set;

public interface Session extends OutputStream<Message> {
    long id();

    void addMethodAdvice(ProbeMethodAdvice advice);

    void clearMethodAdvices();

    void addCachedClass(Class<?> clazz);

    Set<Class<?>> cachedClasses();

    SessionState getState();

    boolean compareAndSet(SessionState expectedState, SessionState newState);

    SessionState setState(SessionState state);

    void synchronize();

    void destroy();
}
