package org.openjava.probe.agent.handler;

import org.openjava.probe.agent.command.*;
import org.openjava.probe.agent.context.Context;
import org.openjava.probe.shared.ErrorCode;
import org.openjava.probe.shared.exception.ProbeServiceException;
import org.openjava.probe.shared.message.codec.PayloadDecoder;
import org.openjava.probe.shared.message.codec.StringPayloadCodec;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class UserCommandHandler extends MessageHandler<Command> {
    private static final Map<String, Class<? extends ProbeCommand>> commands = new HashMap<>();

    static {
        commands.put("monitor", MonitorCommand.class);
        commands.put("cancel", CancelCommand.class);
        commands.put("quit", QuitCommand.class);
    }

    public UserCommandHandler(byte[] payload) {
        super(payload);
    }

    @Override
    public void doHandle(Context context, Command command) {
        command.execute(context);
    }

    @Override
    protected PayloadDecoder<Command> getDecoder() {
        return CommandPayloadDecoder.INSTANCE;
    }

    static class CommandPayloadDecoder implements PayloadDecoder<Command> {
        static final PayloadDecoder<Command> INSTANCE = new CommandPayloadDecoder();
        @Override
        public Command decode(byte[] payload) {
            StringTokenizer tokenizer = new StringTokenizer(StringPayloadCodec.getDecoder().decode(payload));
            int count = tokenizer.countTokens();
            if (count == 0) {
                throw new ProbeServiceException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "Invalid user command");
            }

            String probeCommand = tokenizer.nextToken();
            String[] params = new String[count - 1];
            for (int i = 0; tokenizer.hasMoreElements(); params[i++] = tokenizer.nextToken()) {
            }

            Class<? extends ProbeCommand> clazz = commands.get(probeCommand.toLowerCase());
            if (clazz == null) {
                throw new ProbeServiceException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "unrecognized user command");
            }

            try {
                Constructor<? extends ProbeCommand> constructor = clazz.getConstructor(String[].class);
                return constructor.newInstance(new Object[]{params});
            } catch (InvocationTargetException ex) {
                ex.printStackTrace();
                throw new ProbeServiceException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, ex.getCause().getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new ProbeServiceException(ErrorCode.SYSTEM_UNKNOWN_ERROR, "unknown user command error");
            }
        }
    }
}
