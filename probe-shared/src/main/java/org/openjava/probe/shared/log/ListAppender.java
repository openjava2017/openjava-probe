package org.openjava.probe.shared.log;

public interface ListAppender<T> {
    void addAppender(Appender<T> appender);

    void append(T event);

    void removeAppender(Appender<T> appender);
}
