package org.openjava.probe.agent.asm;

import org.openjava.probe.core.api.ProbeMethod;

public abstract class ProbeTestService {
    public int testProbeAPI(int a, int b) {
        try {
            return a/b;
        } catch (Exception ex) {
            return 0;
        }
    }

    public int testProbeAPI(Integer a,  Integer b) {
        try {
            try {
                return a / b;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } catch (RuntimeException ex) {
            return 0;
        }
    }

    @ProbeMethod(probeId = 10)
    public long testProbeAPI(Long a,  Long b) {
        try {
            return a/b;
        } catch (Exception ex) {
            throw ex;
        }
    }

    public abstract Object testProbeAPI(Object a,  Object b);
}
