package org.openjava.probe.agent.command;

import org.openjava.probe.agent.env.Environment;
import org.openjava.probe.agent.session.Session;

import java.lang.instrument.Instrumentation;

public interface Context {
    Environment environment();

    Instrumentation instrumentation();

    Session session();
}
