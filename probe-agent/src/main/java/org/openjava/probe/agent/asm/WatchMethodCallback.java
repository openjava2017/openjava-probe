package org.openjava.probe.agent.asm;

import org.openjava.probe.agent.advice.MethodAdviceManager;
import org.openjava.probe.agent.advice.MethodPointcut;
import org.openjava.probe.agent.advice.WatchMethodAdvice;
import org.openjava.probe.agent.data.WatchAdviceParam;
import org.openjava.probe.agent.session.Session;

public class WatchMethodCallback implements ProbeCallback {
    private Class clazz;
    private int matchedClasses;
    private int matchedMethods;
    private final Session session;
    private final WatchAdviceParam param;

    public WatchMethodCallback(Session session, WatchAdviceParam param) {
        this.session = session;
        this.param = param;
    }

    @Override
    public void onClassProbe(Class clazz) {
        this.clazz = clazz;
        this.matchedClasses++;
    }

    @Override
    public void onMethodProbe(int probeId, String methodName, String methodDesc) {
        MethodPointcut pointcut = MethodPointcut.of(probeId, clazz, methodName, methodDesc);
        WatchMethodAdvice methodAdvice = new WatchMethodAdvice(pointcut, param);
        methodAdvice.session(session);
        MethodAdviceManager.getInstance().registerMethodAdvice(probeId, methodAdvice);
        session.addMethodAdvice(methodAdvice);
        this.matchedMethods ++;
    }

    public int matchedClasses() {
        return this.matchedClasses;
    }

    public int matchedMethods() {
        return this.matchedMethods;
    }
}
