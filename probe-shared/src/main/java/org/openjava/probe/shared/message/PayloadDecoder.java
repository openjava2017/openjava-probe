package org.openjava.probe.shared.message;

public interface PayloadDecoder<T> {
    T decode(byte[] payload);
}
