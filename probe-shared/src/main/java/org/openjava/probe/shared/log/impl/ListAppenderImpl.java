package org.openjava.probe.shared.log.impl;

import org.openjava.probe.shared.log.Appender;
import org.openjava.probe.shared.log.ListAppender;

import java.util.LinkedList;
import java.util.List;

public class ListAppenderImpl<T> implements ListAppender<T> {
    private volatile boolean needFresh = true;
    private final List<Appender<T>> appenderList = new LinkedList<>();
    private Appender<T>[] appenderArray;

    @Override
    public void append(T event) {
        if (needFresh) {
            synchronized (this) {
                appenderArray = appenderList.toArray(new Appender[0]);
                needFresh = false;
            }
        }

        for (Appender<T> appender : appenderArray) {
            appender.append(event);
        }
    }

    @Override
    public synchronized void addAppender(Appender<T> appender) {
        if (!appenderList.contains(appender)) {
            appenderList.add(appender);
            needFresh = true;
        }
    }

    @Override
    public synchronized void removeAppender(Appender<T> appender) {
        appenderList.remove(appender);
        needFresh = true;
    }
}
