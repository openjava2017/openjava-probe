package org.openjava.probe.agent.advice;

public class MethodPointcut {
    private final int probeId;
    private final Class<?> clazz;
    private final String methodName;
    private final String methodDesc;

    private MethodPointcut(int probeId, Class<?> clazz, String methodName, String methodDesc) {
        this.probeId = probeId;
        this.clazz = clazz;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
    }

    public static MethodPointcut of(int probeId, Class<?> clazz, String methodName, String methodDesc) {
        return new MethodPointcut(probeId, clazz, methodName, methodDesc);
    }

    public int probeId() {
        return this.probeId;
    }

    public Class<?> clazz() {
        return this.clazz;
    }

    public String methodName() {
        return this.methodName;
    }

    public String methodDesc() {
        return this.methodDesc;
    }
}
