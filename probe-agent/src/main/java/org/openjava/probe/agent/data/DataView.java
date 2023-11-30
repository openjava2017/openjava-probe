package org.openjava.probe.agent.data;

import org.openjava.probe.shared.OutputStream;

public interface DataView<T extends DataModel> {
    T data();
    void render(OutputStream<String> output);
}
