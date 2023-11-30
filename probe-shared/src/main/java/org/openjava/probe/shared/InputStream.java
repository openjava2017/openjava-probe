package org.openjava.probe.shared;

public interface InputStream<T> {
    T read();

    void close();
}
