package org.openjava.probe.client.gui.event;

import org.openjava.probe.shared.message.DumpClass;

import java.util.EventObject;

public class DumpEvent extends EventObject {
    private final DumpClass file;

    public DumpEvent(Object source, DumpClass file) {
        super(source);
        this.file = file;
    }

    public DumpClass file() {
        return this.file;
    }
}
