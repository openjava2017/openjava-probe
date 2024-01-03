package org.openjava.probe.shared.message.codec;

public interface PayloadDecoder<T> {
    T decode(byte[] payload);
}
