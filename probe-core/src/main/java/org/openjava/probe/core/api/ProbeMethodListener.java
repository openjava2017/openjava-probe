package org.openjava.probe.core.api;

public interface ProbeMethodListener {
    void onEnterMethod(int probeId, Object[] params);

    void onExitMethod(int probeId, Object[] params, Object returnObject);

    void onExitMethodOnException(int probeId, Object[] params, Throwable ex);

    default void onBeforeInvoke(int probeId, String owner, String name) {
    }

    default void onAfterInvoke(int probeId, String owner, String name) {
    }
}
