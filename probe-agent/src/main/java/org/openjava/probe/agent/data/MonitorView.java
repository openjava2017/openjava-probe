package org.openjava.probe.agent.data;

import org.openjava.probe.shared.OutputStream;

public class MonitorView extends AbstractView<MonitorModel> {
    public MonitorView(MonitorModel data) {
        super(data);
    }

    @Override
    public void render(OutputStream<String> output) {
        MonitorModel data = data();
        //TODO: render data view
        synchronized (data) {
            output.write("monitor data" + data.totalCostInMillis());
        }
    }
}
