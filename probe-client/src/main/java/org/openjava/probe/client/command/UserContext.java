package org.openjava.probe.client.command;

import org.openjava.probe.client.env.Environment;
import org.openjava.probe.client.session.Session;

public class UserContext implements Context {
    private final Environment environment;
    private final Session session;

    private UserContext(Environment environment, Session session) {
        this.environment = environment;
        this.session = session;
    }

    public static Context of(Environment environment, Session session) {
        return new UserContext(environment, session);
    }

    @Override
    public Environment environment() {
        return environment;
    }

    @Override
    public Session session() {
        return session;
    }
}
