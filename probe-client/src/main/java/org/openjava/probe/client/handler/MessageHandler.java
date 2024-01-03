package org.openjava.probe.client.handler;

import org.openjava.probe.client.context.Context;
import org.openjava.probe.shared.message.codec.PayloadDecoder;

public abstract class MessageHandler<T> implements Handler {
    private byte[] payload;

    public MessageHandler(byte[] payload) {
        this.payload = payload;
    }

    public void handle(Context context) {
        doHandle(context, getDecoder().decode(payload));
    }

    protected abstract void doHandle(Context context, T payload);

    protected abstract PayloadDecoder<T> getDecoder();
}
