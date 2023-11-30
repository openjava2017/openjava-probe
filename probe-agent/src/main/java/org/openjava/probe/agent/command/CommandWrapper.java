package org.openjava.probe.agent.command;

import org.openjava.probe.shared.exception.ProbeServiceException;

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
        } catch (ProbeServiceException aex) {
            context.session().write(aex.getMessage());
        } catch (Exception ex) {
            context.session().write("unknown command parse exception");
        }

        if (probeCommand != null) {
            probeCommand.execute(context);
        }
    }
}
