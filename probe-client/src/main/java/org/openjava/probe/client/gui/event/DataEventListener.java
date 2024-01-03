package org.openjava.probe.client.gui.event;

import java.util.EventListener;

public interface DataEventListener extends EventListener {
    void dataChange(DataEvent event);
}
