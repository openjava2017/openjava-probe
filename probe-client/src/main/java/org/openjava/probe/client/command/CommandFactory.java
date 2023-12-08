package org.openjava.probe.client.command;

public interface CommandFactory {
    Command<?> getCommand(int header, byte[] payload) throws Exception;
}
