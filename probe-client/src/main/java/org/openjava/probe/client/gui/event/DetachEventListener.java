package org.openjava.probe.client.gui.event;

import java.util.EventListener;

public interface DetachEventListener extends EventListener {
    void onDetach(DetachEvent event);
}
