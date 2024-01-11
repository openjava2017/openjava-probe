package org.openjava.probe.client;

import org.openjava.probe.client.context.UserEnvironment;
import org.openjava.probe.client.gui.ProbeDashboard;

public class ClientBootstrap {
    public static void main(String[] args) throws Exception {
        new ProbeDashboard(new UserEnvironment(String.join(",", args))).showDashboard();
    }
}
