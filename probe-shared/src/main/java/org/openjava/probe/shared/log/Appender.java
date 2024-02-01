package org.openjava.probe.shared.log;

import org.openjava.probe.shared.ILifeCycle;

public interface Appender<T> extends ILifeCycle {
    void append(T event);
}
