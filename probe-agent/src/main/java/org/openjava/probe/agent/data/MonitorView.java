package org.openjava.probe.agent.data;

import org.openjava.probe.shared.OutputStream;
import org.openjava.probe.shared.message.Message;

public class MonitorView extends AbstractView<MonitorModel> {
    public MonitorView(MonitorModel data) {
        super(data);
    }

    @Override
    public void render(OutputStream<Message> output) {
        MonitorModel data = data();
        //TODO: render data view
        synchronized (data) {
            output.write(Message.ofMessage("monitor data" + data.totalCostInMillis()));
        }
    }
}
