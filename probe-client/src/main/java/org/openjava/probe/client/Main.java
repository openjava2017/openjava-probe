package org.openjava.probe.client;

import org.openjava.probe.client.env.UserEnvironment;
import org.openjava.probe.client.gui.ProbeDashboard;

public class Main {
    public static void main(String[] args) {
        new ProbeDashboard(new UserEnvironment(String.join(",", args)));
    }
}
