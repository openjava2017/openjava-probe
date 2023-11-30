package org.openjava.probe.agent.data;

import org.openjava.probe.shared.OutputStream;

public abstract class AbstractView<T extends AbstractModel> implements DataView<T> {
    private T data;

    public AbstractView(T data) {
        this.data = data;
    }

    @Override
    public T data() {
        return this.data;
    }

    public abstract void render(OutputStream<String> output);
}
