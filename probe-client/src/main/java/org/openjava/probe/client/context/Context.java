package org.openjava.probe.client.context;

import org.openjava.probe.client.context.Environment;
import org.openjava.probe.client.session.Session;

public interface Context {
    Environment environment();

    Session session();
}
