package org.openjava.probe.agent.session;

public interface SessionAware {
    void session(Session session);

    Session session();
}
