package org.openjava.probe.client.gui.event;

import java.util.EventListener;

public interface SessionStateListener extends EventListener {
    void stateChange(SessionStateEvent event);
}
