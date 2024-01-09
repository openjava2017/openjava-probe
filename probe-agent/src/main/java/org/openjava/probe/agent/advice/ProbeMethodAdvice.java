package org.openjava.probe.agent.advice;

import org.openjava.probe.agent.session.Session;
import org.openjava.probe.agent.session.SessionAware;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class ProbeMethodAdvice implements SessionAware {
    private static final AtomicInteger ADVICE_ID = new AtomicInteger(0);

    protected final int id;
    protected final MethodPointcut pointcut;
    protected Session session;

    public ProbeMethodAdvice(MethodPointcut pointcut) {
        this.id = ADVICE_ID.incrementAndGet();
        this.pointcut = pointcut;
    }

    public abstract void onEnterMethod(Object[] params);

    public abstract void onExitMethod(Object[] params, Object returnObject);

    public abstract void onExitMethodOnException(Object[] params, Throwable ex);

    public void destroy() {
    }

    public int id() {
        return this.id;
    }

    public MethodPointcut pointcut() {
        return this.pointcut;
    }

    public void session(Session session) {
        this.session = session;
    }

    public Session session() {
        return this.session;
    }
}
