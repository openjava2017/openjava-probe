package org.openjava.probe.agent.asm;

import org.openjava.probe.agent.advice.MethodAdviceManager;
import org.openjava.probe.agent.advice.MethodPointcut;
import org.openjava.probe.agent.advice.MonitorMethodAdvice;
import org.openjava.probe.agent.data.MonitorAdviceParam;
import org.openjava.probe.agent.session.Session;

public class MonitorMethodCallback implements ProbeCallback {
    private Class<?> clazz;
    private int matchedMethods;
    private final Session session;
    private final MonitorAdviceParam param;

    public MonitorMethodCallback(Session session, MonitorAdviceParam param) {
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
        MonitorMethodAdvice methodAdvice = new MonitorMethodAdvice(pointcut, param);
        methodAdvice.session(session);
        MethodAdviceManager.getInstance().registerMethodAdvice(probeId, methodAdvice);
        session.addMethodAdvice(methodAdvice);
        this.matchedMethods ++;
    }

    @Override
    public Class<?> matchedClass() {
        return this.clazz;
    }

    @Override
    public int matchedMethods() {
        return this.matchedMethods;
    }
}
