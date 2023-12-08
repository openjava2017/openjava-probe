package org.openjava.probe.client.command;

import org.openjava.probe.shared.message.PayloadDecoder;

public abstract class UserCommand<T> implements Command {
    protected T param;

    public UserCommand(byte[] payload) {
        param = getDecoder().decode(payload);
    }

    @Override
    public T param() {
        return param;
    }

    public abstract void execute(Context context);

    public abstract PayloadDecoder<T> getDecoder();
}
