package org.openjava.probe.client.handler;

import org.openjava.probe.client.context.Context;
import org.openjava.probe.client.gui.event.GuiEventMulticaster;
import org.openjava.probe.client.gui.event.SessionStateEvent;
import org.openjava.probe.client.session.SessionState;
import org.openjava.probe.shared.message.codec.IntegerPayloadCodec;
import org.openjava.probe.shared.message.codec.PayloadDecoder;

public class SessionStateHandler extends MessageHandler<SessionState> {
    public SessionStateHandler(byte[] payload) {
        super(payload);
    }

    @Override
    protected void doHandle(Context context, SessionState payload) {
        GuiEventMulticaster.getInstance().fireSessionStateEvent(new SessionStateEvent(this, payload));
    }

    @Override
    protected PayloadDecoder<SessionState> getDecoder() {
        return (bytes -> {
            int state = IntegerPayloadCodec.getDecoder().decode(bytes);
            return SessionState.of(state).get();
        });
    }
}
