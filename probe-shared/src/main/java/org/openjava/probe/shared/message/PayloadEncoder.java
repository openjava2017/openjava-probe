package org.openjava.probe.shared.message;

public interface PayloadEncoder<T> {
    byte[] encode(T payload);
}
