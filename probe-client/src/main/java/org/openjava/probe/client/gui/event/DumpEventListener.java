package org.openjava.probe.client.gui.event;

import java.util.EventListener;

public interface DumpEventListener extends EventListener {
    void onDump(DumpEvent event);
}
