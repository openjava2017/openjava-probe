package org.openjava.probe.agent.command;

import org.openjava.probe.shared.util.AssertUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class ProbeCommandFactory implements CommandFactory {
    private static final Map<String, Class<? extends ProbeCommand>> commands = new HashMap<>();
    private static final CommandFactory instance = new ProbeCommandFactory();

    static {
        registerCommand("monitor", MonitorCommand.class);
        registerCommand("exit", ExitCommand.class);
        registerCommand("quit", QuitCommand.class);
    }

    private static void registerCommand(String command, Class<? extends ProbeCommand> clazz) {
        commands.put(command, clazz);
    }

    private ProbeCommandFactory() {
    }

    public static CommandFactory getInstance() {
        return instance;
    }

    @Override
    public Command<?> getCommand(String command) throws Exception {
        StringTokenizer tokenizer = new StringTokenizer(command);
        int count = tokenizer.countTokens();
        AssertUtils.isTrue(count > 0, "Invalid probe command");

        String probeCommand = tokenizer.nextToken();
        String[] params = new String[count - 1];
        for (int i = 0; tokenizer.hasMoreElements(); params[i++] = tokenizer.nextToken()) {
        }

        Class<? extends ProbeCommand> clazz = commands.get(probeCommand.toLowerCase());
        AssertUtils.notNull(clazz, "unrecognized command");
        Constructor<? extends ProbeCommand> constructor = clazz.getConstructor(String[].class);
        try {
            return constructor.newInstance(new Object[]{params});
        } catch (InvocationTargetException ex) {
            throw (Exception) ex.getCause();
        }
    }
}
