package org.openjava.probe.agent.command;

import org.openjava.probe.agent.context.Context;

public interface Command<T> {

    void execute(Context context);

    T param();

    Class<T> paramClass();

}
