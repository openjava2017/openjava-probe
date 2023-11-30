package org.openjava.probe.agent.command;

public interface Command<T> {

    void execute(Context context);

    T param();

    Class<T> paramClass();

}
