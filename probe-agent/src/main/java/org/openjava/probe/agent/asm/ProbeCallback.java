package org.openjava.probe.agent.asm;

public interface ProbeCallback {
    void onProbe(int probeId, Class clazz, String methodName, String methodDesc);
}
