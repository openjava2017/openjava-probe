package org.openjava.probe.agent.context;

import org.openjava.probe.agent.context.Environment;
import org.openjava.probe.agent.session.Session;

import java.lang.instrument.Instrumentation;

public interface Context {
    Environment environment();

    Instrumentation instrumentation();

    Session session();
}
