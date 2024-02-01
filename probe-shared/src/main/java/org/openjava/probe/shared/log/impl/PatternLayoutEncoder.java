package org.openjava.probe.shared.log.impl;

import org.openjava.probe.shared.log.Encoder;
import org.openjava.probe.shared.log.Layout;
import org.openjava.probe.shared.log.LoggingEvent;

import java.nio.charset.Charset;

public class PatternLayoutEncoder<T> implements Encoder<T> {

    private final Layout<T> layout;

    private final Charset charset;

    public PatternLayoutEncoder(Layout<T> layout, Charset charset) {
        this.layout = layout;
        this.charset = charset;
    }

    @Override
    public byte[] encode(T t) {
        String message = layout.layout(t);
        return charset != null ? message.getBytes(charset) : message.getBytes();
    }
}
