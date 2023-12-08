package org.openjava.probe.client.command;

import org.openjava.probe.shared.message.MessageHeader;
import org.openjava.probe.shared.util.AssertUtils;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class UserCommandFactory implements CommandFactory {
    private static final Map<Integer, Class<? extends UserCommand>> commands = new HashMap<>();
    private static final CommandFactory instance = new UserCommandFactory();

    static {
        registerCommand(MessageHeader.USER_MESSAGE.getCode(), MessageCommand.class);
        registerCommand(MessageHeader.COMMAND_EXIT.getCode(), ExitCommand.class);
    }

    private static void registerCommand(int header, Class<? extends UserCommand> clazz) {
        commands.put(header, clazz);
    }

    private UserCommandFactory() {
    }

    public static CommandFactory getInstance() {
        return instance;
    }

    @Override
    public Command<?> getCommand(int header, byte[] payload) throws Exception {
        Class<? extends UserCommand> clazz = commands.get(header);
        AssertUtils.notNull(clazz, String.format("unrecognized user command: %s", header));
        Constructor<? extends UserCommand> constructor = clazz.getConstructor(byte[].class);
        return constructor.newInstance(new Object[] {payload});
    }
}
