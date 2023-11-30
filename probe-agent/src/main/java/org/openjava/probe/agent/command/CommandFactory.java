package org.openjava.probe.agent.command;

public interface CommandFactory {
    Command<?> getCommand(String command) throws Exception;
}
