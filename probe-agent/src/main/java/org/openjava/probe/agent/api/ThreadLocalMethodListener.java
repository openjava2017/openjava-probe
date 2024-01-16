package org.openjava.probe.agent.api;

import org.openjava.probe.agent.advice.MethodAdviceManager;
import org.openjava.probe.agent.advice.ProbeMethodAdvice;
import org.openjava.probe.core.api.ProbeMethodListener;

import java.util.List;

public class ThreadLocalMethodListener implements ProbeMethodListener {
    private static final ThreadLocal<List<ProbeMethodAdvice>> threadAdvices = new ThreadLocal<>();
    @Override
    public void onEnterMethod(int probeId, Object[] params) {
        List<ProbeMethodAdvice> methodAdvices = MethodAdviceManager.getInstance().queryMethodAdvices(probeId);
        threadAdvices.set(methodAdvices);

        for (ProbeMethodAdvice methodAdvice : methodAdvices) {
            methodAdvice.onEnterMethod(params);
        }
    }

    @Override
    public void onExitMethod(int probeId, Object[] params, Object returnObject) {
        try {
            List<ProbeMethodAdvice> methodAdvices = threadAdvices.get();
            if (methodAdvices == null) {
                methodAdvices = MethodAdviceManager.getInstance().queryMethodAdvices(probeId);
            }

            for (ProbeMethodAdvice methodAdvice : methodAdvices) {
                methodAdvice.onExitMethod(params, returnObject);
            }
        } finally {
            threadAdvices.remove();
        }
    }

    @Override
    public void onExitMethodOnException(int probeId, Object[] params, Throwable ex) {
        try {
            List<ProbeMethodAdvice> methodAdvices = threadAdvices.get();
            if (methodAdvices == null) {
                methodAdvices = MethodAdviceManager.getInstance().queryMethodAdvices(probeId);
            }

            for (ProbeMethodAdvice methodAdvice : methodAdvices) {
                methodAdvice.onExitMethodOnException(params, ex);
            }
        } finally {
            threadAdvices.remove();
        }
    }

    public void onBeforeInvoke(int probeId, String owner, String name) {
        List<ProbeMethodAdvice> methodAdvices = threadAdvices.get();
        if (methodAdvices == null) {
            methodAdvices = MethodAdviceManager.getInstance().queryMethodAdvices(probeId);
        }

        for (ProbeMethodAdvice methodAdvice : methodAdvices) {
            methodAdvice.onBeforeInvoke(owner, name);
        }
    }

    public void onAfterInvoke(int probeId, String owner, String name) {
        List<ProbeMethodAdvice> methodAdvices = threadAdvices.get();
        if (methodAdvices == null) {
            methodAdvices = MethodAdviceManager.getInstance().queryMethodAdvices(probeId);
        }

        for (ProbeMethodAdvice methodAdvice : methodAdvices) {
            methodAdvice.onAfterInvoke(owner, name);
        }
    }
}
