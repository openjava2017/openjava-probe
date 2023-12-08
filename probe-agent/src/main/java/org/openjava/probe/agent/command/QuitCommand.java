package org.openjava.probe.agent.command;

import org.openjava.probe.agent.server.ProbeAgentServer;
import org.openjava.probe.shared.message.Message;

public class QuitCommand extends ProbeCommand<QuitCommand.QuitParam> {
    public QuitCommand(String[] params) throws Exception {
        super(params);
    }

    @Override
    public void execute(Context context) {
        try {
            ProbeAgentServer.getInstance().stop();
        } catch (Exception ex) {
            context.session().write(Message.ofMessage("Quit command execute exception"));
        }
    }

    @Override
    public Class<QuitParam> paramClass() {
        return QuitParam.class;
    }

    static class QuitParam extends ProbeParam {
        public QuitParam(String[] params) {
            super(params);
        }

        @Override
        public void parseParams(String[] params) {
        }
    }
}
