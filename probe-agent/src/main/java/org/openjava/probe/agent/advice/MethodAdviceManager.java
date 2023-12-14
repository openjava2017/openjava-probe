package org.openjava.probe.agent.advice;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//TODO: periodically scan and remove unused and expired advices
public class MethodAdviceManager {
    private static volatile MethodAdviceManager instance;

    private Map<Integer, List<ProbeMethodAdvice>> advices = new ConcurrentHashMap<>();

    private MethodAdviceManager() {
    }

    public static MethodAdviceManager getInstance() {
        if (instance == null) {
            synchronized (MethodAdviceManager.class) {
                if (instance == null) {
                    instance = new MethodAdviceManager();
                }
            }
        }
        return instance;
    }

    public void registerMethodAdvice(int probeId, ProbeMethodAdvice advice) {
        List<ProbeMethodAdvice> adviceList;
        synchronized (advices) {
            adviceList = advices.get(probeId);
            if (adviceList == null) {
                advices.put(probeId, adviceList = new ArrayList<>());
            }
        }
        synchronized (adviceList) {
            adviceList.add(advice);
        }
    }

    public List<ProbeMethodAdvice> queryMethodAdvices(int probeId) {
        List<ProbeMethodAdvice> adviceList = advices.get(probeId);
        if (adviceList == null) {
            return Collections.emptyList();
        }
        synchronized (adviceList) {
            return Collections.unmodifiableList(adviceList);
        }
    }

    public void unregisterMethodAdvice(int probeId, int adviceId) {
        List<ProbeMethodAdvice> adviceList = advices.get(probeId);
        if (adviceList != null) {
            synchronized (adviceList) {
                Iterator<ProbeMethodAdvice> iterator = adviceList.iterator();
                while (iterator.hasNext()) {
                    ProbeMethodAdvice advice = iterator.next();
                    if (advice.id() == adviceId) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    public void clearAllMethodAdvices() {
        advices.clear();
    }

    public void unregisterMethodAdvices(int probeId) {
        advices.remove(probeId);
    }
}
