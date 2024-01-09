package org.openjava.probe.agent.advice;

import org.openjava.probe.agent.data.WatchAdviceParam;
import org.openjava.probe.agent.data.WatchMode;
import org.openjava.probe.agent.json.JsonMapper;
import org.openjava.probe.agent.session.SessionState;
import org.openjava.probe.shared.message.Message;

import java.util.concurrent.atomic.AtomicInteger;

public class WatchMethodAdvice extends ProbeMethodAdvice {

    private final WatchAdviceParam param;
    private final AtomicInteger times = new AtomicInteger(0);

    public WatchMethodAdvice(MethodPointcut pointcut, WatchAdviceParam param) {
        super(pointcut);
        this.param = param;
    }

    @Override
    public void onEnterMethod(Object[] params) {
        if (param.watchMode() == null || param.watchMode() == WatchMode.BEFORE) {
            session.write(Message.info(String.format("method: %s.%s\n    params: %s", pointcut.clazz().getName(),
                pointcut.methodName(), JsonMapper.toJson(params))));

            // TODO: probably maxTimes is not accurate in multi-thread concurrency scenario, but it doesn't matter
            if (times.incrementAndGet() >= param.maxTimes() && session.compareAndSet(SessionState.BUSY, SessionState.IDLE)) {
                session.clearMethodAdvices();
                session.synchronize();
            }
        }
    }

    @Override
    public void onExitMethod(Object[] params, Object returnObject) {
        if (param.watchMode() == WatchMode.AFTER) {
            session.write(Message.info(String.format("method: %s.%s\n    params: %s\n    returnObject: %s",
                pointcut.clazz().getName(), pointcut.methodName(), JsonMapper.toJson(params), returnObject)));

            // probably maxTimes is not accurate in multi-thread concurrency scenario, but it doesn't matter
            if (times.incrementAndGet() >= param.maxTimes() && session.compareAndSet(SessionState.BUSY, SessionState.IDLE)) {
                session.clearMethodAdvices();
                session.synchronize();
            }
        }
    }

    @Override
    public void onExitMethodOnException(Object[] params, Throwable ex) {
        if (param.watchMode() == WatchMode.AFTER) {
            session.write(Message.info(String.format("method: %s.%s\n    params: %s\n    exception: %s",
                pointcut.clazz().getName(), pointcut.methodName(), JsonMapper.toJson(params), ex.getClass().getSimpleName())));

            // probably maxTimes is not accurate in multi-thread concurrency scenario, but it doesn't matter
            if (times.incrementAndGet() >= param.maxTimes() && session.compareAndSet(SessionState.BUSY, SessionState.IDLE)) {
                session.clearMethodAdvices();
                session.synchronize();
            }
        }
    }
}
