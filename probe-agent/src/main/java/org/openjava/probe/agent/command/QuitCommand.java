package org.openjava.probe.agent.command;

import org.openjava.probe.agent.context.Context;
import org.openjava.probe.agent.server.ProbeAgentServer;
import org.openjava.probe.shared.message.Message;

public class QuitCommand extends ProbeCommand<ProbeCommand.NoneParam> {
    public QuitCommand(String[] params) throws Exception {
        super(params);
    }

    @Override
    public void execute(Context context) {
        try {
            ProbeAgentServer.getInstance().stop();
        } catch (Exception ex) {
            context.session().write(Message.error("Quit handler execute exception"));
        }
    }

    @Override
    public Class<NoneParam> paramClass() {
        return NoneParam.class;
    }
}
