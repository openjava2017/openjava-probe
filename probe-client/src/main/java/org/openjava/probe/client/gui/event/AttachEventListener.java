package org.openjava.probe.client.gui.event;

import java.util.EventListener;

public interface AttachEventListener extends EventListener {
    void onAttach(AttachEvent event);
}
