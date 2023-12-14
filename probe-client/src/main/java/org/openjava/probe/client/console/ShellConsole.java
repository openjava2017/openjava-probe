package org.openjava.probe.client.console;

import org.openjava.probe.client.boot.ClientBootstrap;
import org.openjava.probe.client.env.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class ShellConsole {
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

    public Map<String, String> listJavaProcesses() {
        Map<String, String> processes = new LinkedHashMap<>();
        String javaHome = environment.getRequiredProperty("java.home");

        File jps = new File(javaHome, "bin/jps");
        if (!jps.exists()) {
            jps = new File(javaHome, "bin/jps.exe");
            if (!jps.exists()) {
                System.err.println("Cannot find jps command");
                return processes;
            }
        }

        for (String process : run(new String[] {jps.getAbsolutePath(), "-l"})) {
            String[] processInfo = process.split("\\s+");
            if (processInfo.length < 2) {
                continue;
            }
            if (ClientBootstrap.class.getName().equals(processInfo[1]) || processInfo[1].endsWith(".Jps")) {
                continue;
            }
            processes.put(processInfo[0], processInfo[1]);
        }

        return processes;
    }

    public List<String> run(String[] command) {
        Process process;
        try {
            process = Runtime.getRuntime().exec(command);
        } catch (IOException ex) {
            System.err.println("Couldn't run command :" + Arrays.toString(command));
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
}
