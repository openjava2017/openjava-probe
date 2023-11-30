package org.openjava.probe.shared.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ProbeThreadFactory implements ThreadFactory {
    private static volatile ThreadFactory factory;
    private final AtomicInteger idGenerator;
    private final ThreadGroup group;

    private ProbeThreadFactory(String name) {
        this.group = new ThreadGroup(name);
        this.group.setDaemon(true);
        this.group.setMaxPriority(Thread.MIN_PRIORITY);
        this.idGenerator = new AtomicInteger(0);
    }

    public static ThreadFactory getInstance() {
        if (factory == null) {
            synchronized (ProbeThreadFactory.class) {
                if (factory == null) {
                    factory = new ProbeThreadFactory("probe-thread-group");
                }
            }
        }
        return factory;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, "probe-processor-thread-" + idGenerator.incrementAndGet());
        if (!t.isDaemon()) {
            t.setDaemon(true);
        }

        if (t.getPriority() != Thread.MIN_PRIORITY) {
            t.setPriority(Thread.MIN_PRIORITY);
        }

        return t;
    }
}
