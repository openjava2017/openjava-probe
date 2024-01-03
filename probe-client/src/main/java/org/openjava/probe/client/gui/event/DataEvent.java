package org.openjava.probe.client.gui.event;

import org.openjava.probe.shared.message.InfoMessage;

import java.util.EventObject;

public class DataEvent extends EventObject {
    private final InfoMessage message;

    public DataEvent(Object source, InfoMessage message) {
        super(source);
        this.message = message;
    }

    public InfoMessage message() {
        return this.message;
    }

}
