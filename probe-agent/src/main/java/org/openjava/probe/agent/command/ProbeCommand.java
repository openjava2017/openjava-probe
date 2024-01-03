package org.openjava.probe.agent.command;

import org.openjava.probe.agent.context.Context;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class ProbeCommand<T extends ProbeCommand.ProbeParam> implements Command<T> {

    protected final T param;

    public ProbeCommand(String[] params) throws Exception {
        Constructor<T> constructor = paramClass().getConstructor(String[].class);
        try {
            this.param = constructor.newInstance(new Object[]{params});
        } catch (InvocationTargetException tex) {
            throw (Exception) tex.getCause();
        }
    }

    public abstract void execute(Context context);

    public T param() {
        return param;
    }

    static abstract class ProbeParam {
        public ProbeParam(String[] params) {
            parseParams(params);
        }

        public abstract void parseParams(String[] params);
    }

    static class NoneParam extends ProbeParam {
        public NoneParam(String[] params) {
            super(params);
        }

        @Override
        public void parseParams(String[] params) {
        }
    }
}
