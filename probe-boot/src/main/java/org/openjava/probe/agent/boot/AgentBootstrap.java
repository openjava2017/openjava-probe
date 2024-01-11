package org.openjava.probe.agent.boot;

import org.openjava.probe.agent.classloader.AgentClassloader;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.security.CodeSource;
import java.util.jar.JarFile;

public class AgentBootstrap {
    private static ClassLoader agentClassLoader;

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
                probeApiClass = classLoader.loadClass(Constants.PROBE_API_CLASS);
            } catch (Throwable ex) {
                // ignore
            }
        }

        if (probeApiClass == null) {
            CodeSource codeSource = AgentBootstrap.class.getProtectionDomain().getCodeSource();
            File bootJarFile = new File(codeSource.getLocation().toURI());
            File coreJarFile = new File(bootJarFile.getParentFile(), Constants.PROBE_CORE_JAR);
            if (coreJarFile.exists()) {
                instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(coreJarFile));
            } else {
                throw new IllegalAccessException(coreJarFile.getAbsolutePath() + " not found");
            }
        }
    }

    private static void startProbeServer(String args, Instrumentation instrumentation) throws Exception {
        ClassLoader agentClassLoader = initAgentClassLoader();

        Class<?> bootstrapClass = agentClassLoader.loadClass(Constants.PROBE_SERVER_CLASS);
        Object bootstrap = bootstrapClass.getMethod(Constants.PROBE_SERVER_GET_INSTANCE, String.class, Instrumentation.class)
            .invoke(null, args, instrumentation);
        bootstrapClass.getMethod(Constants.PROBE_SERVER_START).invoke(bootstrap);
        boolean isStarted = (boolean) bootstrapClass.getMethod(Constants.PROBE_SERVER_IS_STARTED).invoke(bootstrap);
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

            File agentJarFile = new File(bootJarFile.getParentFile(), Constants.PROBE_AGENT_JAR);
            if (!agentJarFile.exists()) {
                throw new IllegalAccessException(Constants.PROBE_AGENT_JAR + " not found");
            }

            File sharedJarFile = new File(bootJarFile.getParentFile(), Constants.PROBE_SHARED_JAR);
            if (!sharedJarFile.exists()) {
                throw new IllegalAccessException(Constants.PROBE_SHARED_JAR + " not found");
            }

            agentClassLoader = new AgentClassloader(new URL[]{agentJarFile.toURI().toURL(), sharedJarFile.toURI().toURL()});
        }
        return agentClassLoader;
    }
}
