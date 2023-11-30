package org.openjava.probe.shared;

public interface OutputStream<T> {
    void write(T t);

    void close();
}
