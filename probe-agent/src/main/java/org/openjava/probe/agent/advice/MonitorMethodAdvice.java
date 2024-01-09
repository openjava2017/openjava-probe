package org.openjava.probe.agent.advice;

import org.openjava.probe.agent.data.MonitorAdviceParam;
import org.openjava.probe.agent.data.MonitorModel;
import org.openjava.probe.agent.data.MonitorView;
import org.openjava.probe.agent.session.SessionState;
import org.openjava.probe.shared.message.Message;

public class MonitorMethodAdvice extends ProbeMethodAdvice {

    private static final ThreadLocal<Long> TIME_THREAD_LOCAL = new ThreadLocal<>();

    private final MonitorModel data;
    private final MonitorAdviceParam param;

    public MonitorMethodAdvice(MethodPointcut pointcut, MonitorAdviceParam param) {
        super(pointcut);
        this.data = new MonitorModel();
        this.param = param;
    }

    @Override
    public void onEnterMethod(Object[] params) {
        TIME_THREAD_LOCAL.set(System.currentTimeMillis());
    }

    @Override
    public void onExitMethod(Object[] params, Object returnObject) {
        Long start = TIME_THREAD_LOCAL.get();
        TIME_THREAD_LOCAL.remove();
        if (start != null) {
            long costTime = System.currentTimeMillis() - start;
            Message message = Message.info(String.format("%s.%s[success] consumes %s milliseconds",
                pointcut.clazz().getSimpleName(), pointcut.methodName(), costTime));
            session.write(message);
            data.push(true, costTime);

            // TODO: probably maxTimes is not accurate in multi-thread concurrency scenario, but it doesn't matter
            if (param.maxTimes() != null && data.totalTimes() >= param.maxTimes()) {
                if (session.compareAndSet(SessionState.BUSY, SessionState.IDLE)) {
                    session.clearMethodAdvices();
                    session.synchronize();
                }
            }
        }
    }

    @Override
    public void onExitMethodOnException(Object[] params, Throwable ex) {
        Long start = TIME_THREAD_LOCAL.get();
        TIME_THREAD_LOCAL.remove();
        if (start != null) {
            long costTime = System.currentTimeMillis() - start;
            Message message = Message.info(String.format("%s.%s[failed] consumes %s milliseconds",
                pointcut.clazz().getSimpleName(), pointcut.methodName(), costTime));
            session.write(message);
            data.push(false, costTime);

            // probably maxTimes is not accurate in multi-thread concurrency scenario, but it doesn't matter
            if (param.maxTimes() != null && data.totalTimes() >= param.maxTimes()) {
                if (session.compareAndSet(SessionState.BUSY, SessionState.IDLE)) {
                    session.clearMethodAdvices();
                    session.synchronize();
                }
            }
        }
    }

    @Override
    public void destroy() {
        new MonitorView(data).render(session);
    }
}
