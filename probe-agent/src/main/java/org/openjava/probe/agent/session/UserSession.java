package org.openjava.probe.agent.session;

import org.openjava.probe.agent.advice.MethodAdviceManager;
import org.openjava.probe.agent.advice.MethodPointcut;
import org.openjava.probe.agent.advice.ProbeMethodAdvice;
import org.openjava.probe.shared.message.Message;
import org.openjava.probe.shared.message.MessageHeader;
import org.openjava.probe.shared.message.codec.IntegerPayloadCodec;
import org.openjava.probe.shared.nio.session.INioSession;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class UserSession extends SessionOutputAdapter implements Session {
    private final List<ProbeMethodAdvice> advices = new ArrayList<>();
    private final AtomicReference<SessionState> state;
    private final Set<Class<?>> cachedClasses;

    public UserSession(INioSession session) {
        super(session);
        this.state = new AtomicReference<>(SessionState.IDLE);
        this.cachedClasses = new HashSet<>();
    }

    @Override
    public long id() {
        return session.getId();
    }

    @Override
    public void addMethodAdvice(ProbeMethodAdvice advice) {
        synchronized (advices) {
            advices.add(advice);
        }
    }

    @Override
    public void clearMethodAdvices() {
        synchronized (advices) {
            for(ProbeMethodAdvice methodAdvice : advices) {
                MethodPointcut pointcut = methodAdvice.pointcut();
                MethodAdviceManager.getInstance().unregisterMethodAdvice(pointcut.probeId(), methodAdvice.id());
                methodAdvice.destroy();
            }

            advices.clear();
        }
    }

    @Override
    public void write(Message message) {
        if (state.get() != SessionState.CLOSED) {
            super.write(message);
        }
    }

    @Override
    public void addCachedClass(Class<?> clazz) {
        cachedClasses.add(clazz);
    }

    @Override
    public Set<Class<?>> cachedClasses() {
        return cachedClasses;
    }

    @Override
    public SessionState getState() {
        return this.state.get();
    }

    @Override
    public boolean compareAndSet(SessionState expectedState, SessionState newState) {
        return this.state.compareAndSet(expectedState, newState);
    }

    @Override
    public SessionState setState(SessionState state) {
        return this.state.getAndSet(state);
    }

    @Override
    public void synchronize() {
        Message message = Message.of(MessageHeader.SESSION_STATE, this.state.get().code(), IntegerPayloadCodec.getEncoder());
        write(message);
    }

    @Override
    public void destroy() {
        switch(setState(SessionState.CLOSED)) {
            case BUSY:
                clearMethodAdvices();
            case IDLE:
                session.destroy();
                break;
            case CLOSED:
                break;
        }
    }
}
