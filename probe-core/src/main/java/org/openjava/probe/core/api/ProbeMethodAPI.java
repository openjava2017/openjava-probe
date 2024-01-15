package org.openjava.probe.core.api;

public final class ProbeMethodAPI {

    private static final ProbeMethodListener dummyListener = new DummyProbeMethodListener();

    private static volatile ProbeMethodListener listener = dummyListener;

    public static void installMethodListener(ProbeMethodListener methodListener) {
        listener = methodListener;
    }

    public static void reset() {
        listener = dummyListener;
    }

    public static void enterMethod(int probeId, Object[] params) {
        listener.onEnterMethod(probeId, params);
    }

    public static void exitMethod(int probeId, Object[] params, Object returnObject) {
        listener.onExitMethod(probeId, params, returnObject);
    }

    public static void exitMethodOnException(int probeId, Object[] params, Throwable ex) {
        listener.onExitMethodOnException(probeId, params, ex);
    }

    public static void beforeInvoke(int probeId, String owner, String name) {
        listener.onBeforeInvoke(probeId, owner, name);
    }

    public static void afterInvoke(int probeId, String owner, String name) {
        listener.onAfterInvoke(probeId, owner, name);
    }

    static class DummyProbeMethodListener implements ProbeMethodListener {
        @Override
        public void onEnterMethod(int probeId, Object[] params) {

        }

        @Override
        public void onExitMethod(int probeId, Object[] params, Object returnObject) {

        }

        @Override
        public void onExitMethodOnException(int probeId, Object[] params, Throwable ex) {

        }
    }
}