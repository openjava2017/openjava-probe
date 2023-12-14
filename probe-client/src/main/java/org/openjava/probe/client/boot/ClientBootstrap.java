package org.openjava.probe.client.boot;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.openjava.probe.client.agent.ProbeAgentClient;
import org.openjava.probe.client.console.ShellConsole;
import org.openjava.probe.client.env.Environment;
import org.openjava.probe.client.env.UserEnvironment;

import java.io.File;
import java.security.CodeSource;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ClientBootstrap {
    private static final String PROBE_BOOT_JAR = "probe-boot-1.0.0.jar";
    private static final String PROBE_SHARED_JAR = "probe-shared-1.0.0.jar";

    public static void main(String[] args) {
        Environment environment = new UserEnvironment(String.join(",", args));
        ShellConsole console = new ShellConsole(environment);
        Map<String, String> processes = console.listJavaProcesses();

        if (processes.isEmpty()) {
            System.err.println("Cannot find java processes");
            System.exit(1);
        }

        System.out.println(String.format("%s          %s", "pid", "main-class"));
        for (Map.Entry<String, String> entry : processes.entrySet()) {
            System.out.println(String.format("%s          %s", entry.getKey(), entry.getValue()));
        }

        String pid;
        String tips = "Please choose one process above(or exit): ";
        for (pid = console.readLine(tips); processes.get(pid) == null; pid = console.readLine(tips)) {
            if ("exit".equals(pid)) {
                System.exit(0);
            }
        }
        attachAgent(pid, environment);

        ExecutorService executorService = new ThreadPoolExecutor(2, 10, 4, TimeUnit.SECONDS, new ArrayBlockingQueue<>(20));
        ProbeAgentClient client = new ProbeAgentClient(environment, executorService);
        try {
            client.start();
        } catch (Exception ex) {
            System.err.println("Probe agent client start failed");
            ex.printStackTrace(System.err);
            System.exit(1);
        }

        tips = environment.getProperty("user.name") + " $ ";
        // TODO: make sure the client already started(command handle thread inside the client)
        for (String command = console.readLine(tips); command != null; command = console.readLine(tips)) {
            if ("exit".equals(command)) {
                try {
                    client.stop();
                } catch (Exception ex) {
                    System.err.println("Probe agent client stop exception");
                    ex.printStackTrace(System.err);
                    System.exit(1);
                }
                System.exit(0);
            }

            client.onCommand(command);
            try {
                if(!client.requireIdle()) {
                    System.err.println("Illegal user session state, system will exit");
                    break;
                }
            } catch (InterruptedException iex) {
                break;
            }
        }

        try {
            client.stop();
        } catch (Exception ex) {
            System.err.println("The client stop failed");
            ex.printStackTrace(System.err);
            System.exit(1);
        }

        System.exit(0);
    }

    private static void attachAgent(String processId, Environment environment) {
        VirtualMachineDescriptor virtualMachineDescriptor = null;
        for (VirtualMachineDescriptor descriptor : VirtualMachine.list()) {
            if (processId.equals(descriptor.id())) {
                virtualMachineDescriptor = descriptor;
                break;
            }
        }

        File jarHomeFile;
        try {
            CodeSource codeSource = ClientBootstrap.class.getProtectionDomain().getCodeSource();
            jarHomeFile = new File(codeSource.getLocation().toURI()).getParentFile();
        } catch (Exception ex) {
            jarHomeFile = new File(environment.getRequiredProperty("user.dir"));
        }
        File bootJarFile = new File(jarHomeFile, PROBE_BOOT_JAR);
        if (!bootJarFile.exists()) {
            System.err.println(bootJarFile.getAbsolutePath() + " not found");
            System.exit(1);
        }
        File sharedJarFile = new File(jarHomeFile, PROBE_SHARED_JAR);
        if (!sharedJarFile.exists()) {
            System.err.println(sharedJarFile.getAbsolutePath() + " not found");
            System.exit(1);
        }

        VirtualMachine virtualMachine = null;
        try {
            if (virtualMachineDescriptor == null) {
                virtualMachine = VirtualMachine.attach(processId);
            } else {
                virtualMachine = VirtualMachine.attach(virtualMachineDescriptor);
            }

            virtualMachine.loadAgent(bootJarFile.getAbsolutePath(), prepareAgentOptions(environment, jarHomeFile));
        } catch (Exception ex) {
            System.err.println("The agent attach exception");
            ex.printStackTrace(System.err);
            System.exit(1);
        } finally {
            if (virtualMachine != null) {
                try {
                    virtualMachine.detach();
                } catch (Exception iex) {
                    System.err.println("Agent detach failed");
                    iex.printStackTrace(System.err);
                    System.exit(1);
                }
            }
        }
    }

    private static String prepareAgentOptions(Environment environment, File jarHomeFile) {
        File probeWorkHome = new File(jarHomeFile, "probe");
        if (probeWorkHome.exists()) {
            probeWorkHome.mkdir();
        }
        String probeHome = environment.getProperty("probe.work.home", probeWorkHome.getAbsolutePath());
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%s=%s", "probe.work.home", probeHome));

        String probeHost = environment.getProperty("probe.server.host");
        if (probeHost != null) {
            builder.append(String.format(",%s=%s", "probe.server.host", probeHost));
        }

        String probePort = environment.getProperty("probe.server.port");
        if (probeHost != null) {
            builder.append(String.format(",%s=%s", "probe.server.port", probePort));
        }
        return builder.toString();
    }
}
