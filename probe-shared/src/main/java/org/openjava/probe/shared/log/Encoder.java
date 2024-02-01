package org.openjava.probe.shared.log;

public interface Encoder<T> {
    byte[] encode(T t);
}
