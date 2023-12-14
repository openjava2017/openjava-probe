package org.openjava.probe.agent.command;

import org.openjava.probe.shared.message.Message;
import org.openjava.probe.shared.message.MessageHeader;
import org.openjava.probe.shared.message.PayloadHelper;

public class CommandWrapper {
    private Context context;
    private String command;

    private CommandWrapper(Context context, String command) {
        this.context = context;
        this.command = command;
    }

    public static CommandWrapper of(Context context, String command) {
        return new CommandWrapper(context, command);
    }

    public void execute() {

        Command probeCommand = null;
        try {
            probeCommand = ProbeCommandFactory.getInstance().getCommand(command);
        } catch (IllegalArgumentException ex) {
            context.session().write(Message.of(MessageHeader.COMMAND_EXIT, ex.getMessage(), PayloadHelper.STRING_ENCODER));
        } catch (Exception ex) {
            context.session().write(Message.of(MessageHeader.COMMAND_EXIT, "unknown command parse error", PayloadHelper.STRING_ENCODER));
        }

        if (probeCommand != null) {
            probeCommand.execute(context);
        }
    }
}
