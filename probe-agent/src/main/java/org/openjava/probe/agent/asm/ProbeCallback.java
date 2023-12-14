package org.openjava.probe.agent.asm;

public interface ProbeCallback {
    void onClassProbe(Class clazz);

    void onMethodProbe(int probeId, String methodName, String methodDesc);
}
