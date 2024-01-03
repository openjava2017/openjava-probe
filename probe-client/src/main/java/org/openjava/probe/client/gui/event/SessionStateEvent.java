package org.openjava.probe.client.gui.event;

import org.openjava.probe.client.session.SessionState;

import java.util.EventObject;

public class SessionStateEvent extends EventObject {
    private final SessionState state;

    public SessionStateEvent(Object source, SessionState state) {
        super(source);
        this.state = state;
    }

    public SessionState state() {
        return this.state;
    }

}
