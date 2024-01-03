package org.openjava.probe.client.handler;

import org.openjava.probe.client.context.Context;
import org.openjava.probe.client.gui.event.DataEvent;
import org.openjava.probe.client.gui.event.GuiEventMulticaster;
import org.openjava.probe.shared.message.InfoMessage;
import org.openjava.probe.shared.message.codec.InfoPayloadCodec;
import org.openjava.probe.shared.message.codec.PayloadDecoder;

public class UserMessageHandler extends MessageHandler<InfoMessage> {
    public UserMessageHandler(byte[] payload) {
        super(payload);
    }

    @Override
    protected void doHandle(Context context, InfoMessage payload) {
        GuiEventMulticaster.getInstance().fireDataEvent(new DataEvent(this, payload));
    }

    @Override
    public PayloadDecoder<InfoMessage> getDecoder() {
        return InfoPayloadCodec.getDecoder();
    }
}
