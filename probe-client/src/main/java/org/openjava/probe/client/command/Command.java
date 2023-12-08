package org.openjava.probe.client.command;

import org.openjava.probe.shared.message.PayloadDecoder;

public interface Command<T> {

    T param();

    void execute(Context context);

    PayloadDecoder<T> getDecoder();
}
