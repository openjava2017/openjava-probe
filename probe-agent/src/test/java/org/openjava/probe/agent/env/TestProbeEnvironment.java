package org.openjava.probe.agent.env;

public class TestProbeEnvironment {
    public static void main(String[] args) {
        Environment environment = new ProbeEnvironment("key1=value1");
        String s = environment.getProperty("probe.server.host");
        System.out.println(s);
        Integer port = environment.getProperty("probe.server.port", int.class);
        System.out.println(port);
        String userHome = environment.getProperty("probe.work.home");
        System.out.println(userHome);
        String value = environment.getProperty("key1");
        System.out.println(value);
    }
}
