package org.openjava.probe.agent.session;

import org.openjava.probe.agent.advice.MethodAdviceManager;
import org.openjava.probe.agent.advice.MethodPointcut;
import org.openjava.probe.agent.advice.ProbeMethodAdvice;
import org.openjava.probe.shared.nio.session.INioSession;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class UserSession extends SessionOutputAdapter implements Session {
    private final List<ProbeMethodAdvice> advices = new ArrayList<>();
    private final AtomicReference<SessionState> state;

    public UserSession(INioSession session) {
        super(session);
        this.state = new AtomicReference<>(SessionState.IDLE);
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
    public boolean compareAndSet(SessionState expectedState, SessionState newState) {
        return this.state.compareAndSet(expectedState, newState);
    }

    @Override
    public SessionState setState(SessionState state) {
        return this.state.getAndSet(state);
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
