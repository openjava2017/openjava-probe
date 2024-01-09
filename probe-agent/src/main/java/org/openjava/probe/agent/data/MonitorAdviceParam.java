package org.openjava.probe.agent.data;

public class MonitorAdviceParam {
    private final Integer maxTimes;

    private MonitorAdviceParam(Integer maxTimes) {
        this.maxTimes = maxTimes;
    }

    public static MonitorAdviceParam of(Integer maxTimes) {
        return new MonitorAdviceParam(maxTimes);
    }

    public Integer maxTimes() {
        return this.maxTimes;
    }
}
