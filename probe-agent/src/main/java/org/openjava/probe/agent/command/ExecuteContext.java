package org.openjava.probe.agent.command;

import org.openjava.probe.agent.env.Environment;
import org.openjava.probe.agent.session.Session;

import java.lang.instrument.Instrumentation;

public class ExecuteContext implements Context {
    private final Environment environment;
    private final Instrumentation instrumentation;
    private final Session session;

    private ExecuteContext(Environment environment, Instrumentation instrumentation, Session session) {
        this.environment = environment;
        this.instrumentation = instrumentation;
        this.session = session;
    }

    public static Context of(Environment environment, Instrumentation instrumentation, Session session) {
        return new ExecuteContext(environment, instrumentation, session);
    }

    @Override
    public Environment environment() {
        return environment;
    }

    @Override
    public Instrumentation instrumentation() {
        return instrumentation;
    }

    @Override
    public Session session() {
        return session;
    }
}
