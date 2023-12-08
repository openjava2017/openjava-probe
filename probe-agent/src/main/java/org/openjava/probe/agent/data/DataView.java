package org.openjava.probe.agent.data;

import org.openjava.probe.shared.OutputStream;
import org.openjava.probe.shared.message.Message;

public interface DataView<T extends DataModel> {
    T data();
    void render(OutputStream<Message> output);
}
