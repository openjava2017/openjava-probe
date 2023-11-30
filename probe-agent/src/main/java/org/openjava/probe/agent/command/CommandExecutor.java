package org.openjava.probe.agent.command;

import org.openjava.probe.shared.ILifeCycle;

public interface CommandExecutor extends ILifeCycle {
    void submit(CommandWrapper command);
}
