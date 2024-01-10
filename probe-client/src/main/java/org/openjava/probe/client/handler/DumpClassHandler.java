package org.openjava.probe.client.handler;

import org.openjava.probe.client.context.Context;
import org.openjava.probe.client.gui.event.DumpEvent;
import org.openjava.probe.client.gui.event.GuiEventMulticaster;
import org.openjava.probe.shared.message.DumpClass;
import org.openjava.probe.shared.message.codec.ClassPayloadCodec;
import org.openjava.probe.shared.message.codec.PayloadDecoder;

public class DumpClassHandler extends MessageHandler<DumpClass> {
    public DumpClassHandler(byte[] payload) {
        super(payload);
    }

    @Override
    protected void doHandle(Context context, DumpClass payload) {
        GuiEventMulticaster.getInstance().fireDumpEvent(new DumpEvent(this, payload));
    }

    @Override
    protected PayloadDecoder<DumpClass> getDecoder() {
        return (bytes -> ClassPayloadCodec.getDecoder().decode(bytes));
    }
}
