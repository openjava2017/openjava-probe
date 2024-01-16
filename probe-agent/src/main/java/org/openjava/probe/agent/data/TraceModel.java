package org.openjava.probe.agent.data;

import java.util.ArrayList;
import java.util.List;

public class TraceModel extends AbstractModel {
    private String name;
    private long costInMillis;
    private List<TraceModel> methods;

    public TraceModel(String name, long costInMillis) {
        this.name = name;
        this.costInMillis = costInMillis;
    }

    public void costInMillis(long costInMillis) {
        this.costInMillis = costInMillis;
    }

    public void addTraceModel(TraceModel model) {
        if (this.methods == null) {
            this.methods = new ArrayList<>();
        }

        this.methods.add(model);
    }

    @Override
    public void clear() {
        this.costInMillis = -1;
        methods.clear();
    }
}
