package org.openjava.probe.client.agent;

import org.openjava.probe.shared.ILifeCycle;

public interface UserCommandListener extends ILifeCycle {
    void onCommand(String command);
}
