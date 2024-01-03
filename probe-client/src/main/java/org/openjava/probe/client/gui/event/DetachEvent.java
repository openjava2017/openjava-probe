package org.openjava.probe.client.gui.event;

import org.openjava.probe.client.session.Session;

import java.util.EventObject;

public class DetachEvent extends EventObject {
    private final Session session;

    public DetachEvent(Object source, Session session) {
        super(source);
        this.session = session;
    }

    public Session session() {
        return this.session;
    }
}
