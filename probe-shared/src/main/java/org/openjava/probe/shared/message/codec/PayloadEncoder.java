package org.openjava.probe.shared.message.codec;

public interface PayloadEncoder<T> {
    byte[] encode(T payload);
}
