package org.openjava.probe.client.command;

import org.openjava.probe.client.env.Environment;
import org.openjava.probe.client.session.Session;

public interface Context {
    Environment environment();

    Session session();
}
