package org.openjava.probe.agent.asm;

import org.openjava.probe.agent.advice.MethodAdviceManager;
import org.openjava.probe.agent.advice.MethodPointcut;
import org.openjava.probe.agent.advice.MonitorMethodAdvice;
import org.openjava.probe.agent.advice.TraceMethodAdvice;
import org.openjava.probe.agent.data.MonitorAdviceParam;
import org.openjava.probe.agent.data.TraceAdviceParam;
import org.openjava.probe.agent.session.Session;

public class TraceMethodCallback implements ProbeCallback {
    private Class<?> clazz;
    private final Session session;
    private final TraceAdviceParam param;

    public TraceMethodCallback(Session session, TraceAdviceParam param) {
        this.session = session;
        this.param = param;
    }

    @Override
    public void onClassProbe(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void onMethodProbe(int probeId, String methodName, String methodDesc) {
        MethodPointcut pointcut = MethodPointcut.of(probeId, clazz, methodName, methodDesc);
        TraceMethodAdvice methodAdvice = new TraceMethodAdvice(pointcut, param);
        methodAdvice.session(session);
        MethodAdviceManager.getInstance().registerMethodAdvice(probeId, methodAdvice);
        session.addMethodAdvice(methodAdvice);
    }
}
