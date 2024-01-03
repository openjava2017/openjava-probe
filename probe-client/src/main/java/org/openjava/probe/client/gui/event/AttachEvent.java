package org.openjava.probe.client.gui.event;

import java.util.EventObject;

public class AttachEvent extends EventObject {
    private final boolean success;

    public AttachEvent(Object source, boolean success) {
        super(source);
        this.success = success;
    }

    public boolean success() {
        return this.success;
    }
}
