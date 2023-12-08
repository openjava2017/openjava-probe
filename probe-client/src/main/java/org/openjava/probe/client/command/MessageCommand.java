package org.openjava.probe.client.command;

import org.openjava.probe.shared.message.PayloadDecoder;
import org.openjava.probe.shared.message.PayloadHelper;

public class MessageCommand extends UserCommand<String> {
    public MessageCommand(byte[] payload) {
        super(payload);
    }

    @Override
    public void execute(Context context) {

    }

    @Override
    public PayloadDecoder<String> getDecoder() {
        return PayloadHelper.STRING_DECODER;
    }
}
