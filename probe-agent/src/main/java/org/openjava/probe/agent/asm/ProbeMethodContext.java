package org.openjava.probe.agent.asm;

import java.util.List;

public class ProbeMethodContext implements ProbeCallback {

    private final ProbeCallback callback;

    private final List<String> traceMethods;

    private Class<?> clazz;

    private int matchedMethods;

    private ProbeMethodContext(ProbeCallback callback, List<String> traceMethods) {
        this.callback = callback;
        this.traceMethods = traceMethods;
    }

    public static ProbeMethodContext of(ProbeCallback callback) {
        return new ProbeMethodContext(callback, null);
    }

    public static ProbeMethodContext of(List<String> traceMethods, ProbeCallback callback) {
        return new ProbeMethodContext(callback, traceMethods);
    }

    public void onClassProbe(Class<?> clazz) {
        this.callback.onClassProbe(clazz);
        this.clazz = clazz;
    }

    @Override
    public void onMethodProbe(int probeId, String methodName, String methodDesc) {
        this.callback.onMethodProbe(probeId, methodName, methodDesc);
    }

    public List<String> traceMethods() {
        return this.traceMethods;
    }

    public Class<?> matchedClass() {
        return this.clazz;
    }

    public void incMatchedMethods() {
        this.matchedMethods ++;
    }

    public int matchedMethods() {
        return matchedMethods;
    }
}