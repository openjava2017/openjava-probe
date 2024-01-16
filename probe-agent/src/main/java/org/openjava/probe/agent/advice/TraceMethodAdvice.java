package org.openjava.probe.agent.advice;

import org.openjava.probe.agent.data.TraceAdviceParam;
import org.openjava.probe.agent.data.TraceModel;
import org.openjava.probe.agent.json.JsonMapper;
import org.openjava.probe.agent.session.SessionState;
import org.openjava.probe.shared.message.Message;

import java.util.concurrent.atomic.AtomicInteger;

public class TraceMethodAdvice extends ProbeMethodAdvice {

    private static final ThreadLocal<TraceModel> DATA_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<Long> TIME_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<Long> METHOD_THREAD_LOCAL = new ThreadLocal<>();
    private final AtomicInteger times = new AtomicInteger(0);

    private final TraceAdviceParam param;

    public TraceMethodAdvice(MethodPointcut pointcut, TraceAdviceParam param) {
        super(pointcut);
        this.param = param;
    }

    @Override
    public void onEnterMethod(Object[] params) {
        String methodName = String.format("%s.%s", pointcut.clazz().getSimpleName(), pointcut.methodName());
        DATA_THREAD_LOCAL.set(new TraceModel(methodName, -1));
        TIME_THREAD_LOCAL.set(System.currentTimeMillis());
    }

    @Override
    public void onExitMethod(Object[] params, Object returnObject) {
        finish();
    }

    @Override
    public void onExitMethodOnException(Object[] params, Throwable ex) {
        finish();
    }

    @Override
    public void onBeforeInvoke(String owner, String name) {
        METHOD_THREAD_LOCAL.set(System.currentTimeMillis());
    }

    @Override
    public void onAfterInvoke(String owner, String name) {
        String className = owner.substring(owner.lastIndexOf("/") + 1);
        long costInMillis = System.currentTimeMillis() - METHOD_THREAD_LOCAL.get();
        TraceModel data = DATA_THREAD_LOCAL.get();
        data.addTraceModel(new TraceModel(String.format("%s.%s", className, name), costInMillis));
    }

    private void finish() {
        TraceModel data = DATA_THREAD_LOCAL.get();
        data.costInMillis(System.currentTimeMillis() - TIME_THREAD_LOCAL.get());
        DATA_THREAD_LOCAL.remove();
        TIME_THREAD_LOCAL.remove();

        session.write(Message.info(JsonMapper.toJson(data)));
        if (param.maxTimes() != null && times.incrementAndGet() >= param.maxTimes() && session.compareAndSet(SessionState.BUSY, SessionState.IDLE)) {
            session.clearMethodAdvices();
            session.synchronize();
        }
    }
}
