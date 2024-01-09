package org.openjava.probe.agent.data;

public class WatchAdviceParam {
    private final WatchMode watchMode;
    private final Integer maxTimes;

    private WatchAdviceParam(WatchMode watchMode, Integer maxTimes) {
        this.watchMode = watchMode;
        this.maxTimes = maxTimes;
    }

    public static WatchAdviceParam of(WatchMode watchMode, Integer maxTimes) {
        return new WatchAdviceParam(watchMode, maxTimes);
    }

    public Integer maxTimes() {
        return this.maxTimes;
    }

    public WatchMode watchMode() {
        return this.watchMode;
    }
}
