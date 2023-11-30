package org.openjava.probe.agent.boot;

import java.lang.instrument.Instrumentation;

public class AgentBootstrp {
    public static void premain(String args, Instrumentation inst) {
        main(args, inst);
    }

    public static void agentmain(String args, Instrumentation inst) {
        main(args, inst);
    }

    private static void main(String args, Instrumentation inst) {

    }
}
