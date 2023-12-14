package org.openjava.probe.agent.boot;

import org.openjava.probe.agent.classloader.AgentClassloader;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.security.CodeSource;
import java.util.jar.JarFile;

public class AgentBootstrap {
    private static ClassLoader agentClassLoader;
    private static final String PROBE_AGENT_JAR = "probe-agent-1.0.0.jar";
    private static final String PROBE_SHARED_JAR = "probe-shared-1.0.0.jar";
    private static final String PROBE_CORE_JAR = "probe-core-1.0.0.jar";
    private static final String PROBE_API_CLASS = "org.openjava.probe.core.api.ProbeMethodAPI";
    private static final String PROBE_SERVER_CLASS = "org.openjava.probe.agent.server.ProbeAgentServer";
    private static final String PROBE_SERVER_GET_INSTANCE = "getInstance";
    private static final String PROBE_SERVER_START = "start";
    private static final String PROBE_SERVER_IS_STARTED = "isStarted";


    public static void premain(String args, Instrumentation instrumentation) {
        main(args, instrumentation);
    }

    public static void agentmain(String args, Instrumentation instrumentation) {
        main(args, instrumentation);
    }

    private static void main(String args, Instrumentation instrumentation) {
        try {
            initProbeApi(instrumentation);
        } catch (Exception ex) {
            System.err.println("Probe agent API init failed");
            ex.printStackTrace(System.err);
            throw new RuntimeException("Probe agent API init failed", ex);
        }

        try {
            startProbeServer(args, instrumentation);
        } catch (Exception ex) {
            System.err.println("Start probe agent server failed");
            ex.printStackTrace(System.err);
            throw new RuntimeException("Start probe agent server failed", ex);
        }
    }

    private static void initProbeApi(Instrumentation instrumentation) throws Exception {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        Class<?> probeApiClass = null;
        if (classLoader != null) {
            try {
                probeApiClass = classLoader.loadClass(PROBE_API_CLASS);
            } catch (Throwable ex) {
                // ignore
            }
        }

        if (probeApiClass == null) {
            CodeSource codeSource = AgentBootstrap.class.getProtectionDomain().getCodeSource();
            File bootJarFile = new File(codeSource.getLocation().toURI());
            File coreJarFile = new File(bootJarFile.getParentFile(), PROBE_CORE_JAR);
            if (coreJarFile.exists()) {
                instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(coreJarFile));
            } else {
                throw new IllegalAccessException(coreJarFile.getAbsolutePath() + " not found");
            }
        }
    }

    private static void startProbeServer(String args, Instrumentation instrumentation) throws Exception {
        ClassLoader agentClassLoader = initAgentClassLoader();

        Class<?> bootstrapClass = agentClassLoader.loadClass(PROBE_SERVER_CLASS);
        Object bootstrap = bootstrapClass.getMethod(PROBE_SERVER_GET_INSTANCE, String.class, Instrumentation.class).invoke(null, args, instrumentation);
        bootstrapClass.getMethod(PROBE_SERVER_START).invoke(bootstrap);
        boolean isStarted = (boolean) bootstrapClass.getMethod(PROBE_SERVER_IS_STARTED).invoke(bootstrap);
        if (isStarted) {
            System.out.println("Probe agent server started");
        } else {
            System.err.println("Probe agent server start failed");
        }
    }

    private synchronized static ClassLoader initAgentClassLoader() throws Exception {
        if (agentClassLoader == null) {
            CodeSource codeSource = AgentBootstrap.class.getProtectionDomain().getCodeSource();
            File bootJarFile = new File(codeSource.getLocation().toURI());

            File agentJarFile = new File(bootJarFile.getParentFile(), PROBE_AGENT_JAR);
            if (!agentJarFile.exists()) {
                throw new IllegalAccessException(PROBE_AGENT_JAR + " not found");
            }

            File sharedJarFile = new File(bootJarFile.getParentFile(), PROBE_SHARED_JAR);
            if (!sharedJarFile.exists()) {
                throw new IllegalAccessException(PROBE_SHARED_JAR + " not found");
            }

            agentClassLoader = new AgentClassloader(new URL[]{agentJarFile.toURI().toURL(), sharedJarFile.toURI().toURL()});
        }
        return agentClassLoader;
    }
}
