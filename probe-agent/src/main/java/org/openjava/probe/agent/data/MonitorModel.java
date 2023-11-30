package org.openjava.probe.agent.data;

import java.time.LocalDateTime;

public class MonitorModel extends AbstractModel {
    private LocalDateTime startTime;
    private int totalTimes;
    private int successTimes;
    private int failedTimes;
    private long totalCostInMillis;
    private long avgCostInMillis;
    private long maxCostInMillis;
    private long minCostInMillis;

    public MonitorModel() {
        startTime = LocalDateTime.now();
    }

    public synchronized void push(boolean success, long costInMillis) {
        totalTimes++;
        if (success) {
            successTimes++;

            totalCostInMillis += costInMillis;
            avgCostInMillis = totalCostInMillis / totalTimes;
            if (costInMillis > maxCostInMillis) {
                maxCostInMillis = costInMillis;
            }
            if (costInMillis < minCostInMillis) {
                minCostInMillis = costInMillis;
            }
        } else {
            failedTimes++;
        }
    }

    @Override
    public synchronized void clear() {
        totalTimes = 0;
        successTimes = 0;
        failedTimes = 0;
        totalCostInMillis = 0;
        avgCostInMillis = 0;
        maxCostInMillis = 0;
        minCostInMillis = 0;
    }

    public LocalDateTime startTime() {
        return startTime;
    }

    public int totalTimes() {
        return totalTimes;
    }

    public int successTimes() {
        return successTimes;
    }

    public int failedTimes() {
        return failedTimes;
    }

    public long totalCostInMillis() {
        return totalCostInMillis;
    }

    public long avgCostInMillis() {
        return avgCostInMillis;
    }

    public long maxCostInMillis() {
        return maxCostInMillis;
    }

    public long minCostInMillis() {
        return minCostInMillis;
    }
}
