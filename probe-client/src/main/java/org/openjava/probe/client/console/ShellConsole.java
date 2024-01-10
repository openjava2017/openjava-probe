package org.openjava.probe.client.console;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.openjava.probe.client.boot.ClientBootstrap;
import org.openjava.probe.client.context.Environment;
import org.openjava.probe.shared.ErrorCode;
import org.openjava.probe.shared.exception.ProbeServiceException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.CodeSource;
import java.util.*;

public class ShellConsole {
    private static final String PROBE_BOOT_JAR = "probe-boot-1.0.0.jar";
    private static final String PROBE_SHARED_JAR = "probe-shared-1.0.0.jar";

    private final Environment environment;

    public ShellConsole(Environment environment) {
        this.environment = environment;
    }

    public String readLine(String tips) {
        if (tips != null) {
            System.out.print(tips);
        }

        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    public List<JavaProcess> listJavaProcesses() {
        String javaHome = environment.getRequiredProperty("java.home");

        File jps = new File(javaHome, "bin/jps");
        if (!jps.exists()) {
            jps = new File(javaHome, "bin/jps.exe");
            if (!jps.exists()) {
                System.err.println("Cannot find jps handler");
                return Collections.emptyList();
            }
        }

        List<JavaProcess> processes = new ArrayList<>();
        for (String process : run(new String[] {jps.getAbsolutePath(), "-l"})) {
            String[] processInfo = process.split("\\s+");
            if (processInfo.length < 2) {
                continue;
            }
            if (processInfo[1].startsWith("probe-client") || processInfo[1].endsWith(".Jps")) {
                continue;
            }
            processes.add(JavaProcess.of(processInfo[0], processInfo[1]));
        }

        return processes;
    }

    public List<String> run(String[] command) {
        Process process;
        try {
            process = Runtime.getRuntime().exec(command);
        } catch (IOException ex) {
            System.err.println("Couldn't run handler :" + Arrays.toString(command));
            ex.printStackTrace(System.err);
            return Collections.emptyList();
        }

        List<String> response = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                response.add(line);
            }
            process.waitFor();
        } catch (IOException ex) {
            System.err.println("Problem reading output from :" + Arrays.toString(command));
            ex.printStackTrace(System.err);
        } catch (InterruptedException iex) {
            iex.printStackTrace(System.err);
        }
        return response;
    }

    public void attachAgent(String processId) {
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
            throw new ProbeServiceException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, bootJarFile.getAbsolutePath() + " not found");
        }
        File sharedJarFile = new File(jarHomeFile, PROBE_SHARED_JAR);
        if (!sharedJarFile.exists()) {
            System.err.println(sharedJarFile.getAbsolutePath() + " not found");
            throw new ProbeServiceException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, sharedJarFile.getAbsolutePath() + " not found");
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
            ex.printStackTrace(System.err);
            throw new ProbeServiceException(ErrorCode.SYSTEM_UNKNOWN_ERROR, "The agent attach exception");
        } finally {
            if (virtualMachine != null) {
                try {
                    virtualMachine.detach();
                } catch (Exception iex) {
                    iex.printStackTrace(System.err);
                    throw new ProbeServiceException(ErrorCode.SYSTEM_UNKNOWN_ERROR, "The agent detach failed");
                }
            }
        }
    }

    private String prepareAgentOptions(Environment environment, File jarHomeFile) {
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
