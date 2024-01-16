package org.openjava.probe.agent.data;

public class TraceAdviceParam {
    private final Integer maxTimes;

    private TraceAdviceParam(Integer maxTimes) {
        this.maxTimes = maxTimes;
    }

    public static TraceAdviceParam of(Integer maxTimes) {
        return new TraceAdviceParam(maxTimes);
    }

    public Integer maxTimes() {
        return this.maxTimes;
    }
}
