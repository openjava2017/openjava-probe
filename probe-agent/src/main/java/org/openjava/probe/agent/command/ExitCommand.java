package org.openjava.probe.agent.command;

import org.openjava.probe.agent.session.Session;
import org.openjava.probe.agent.session.SessionState;

public class ExitCommand extends ProbeCommand<ProbeCommand.NoneParam> {
    public ExitCommand(String[] params) throws Exception {
        super(params);
    }

    @Override
    public void execute(Context context) {
        Session session = context.session();
        if (session.setState(SessionState.IDLE) == SessionState.BUSY) {
            session.clearMethodAdvices();
        }
    }

    @Override
    public Class<NoneParam> paramClass() {
        return NoneParam.class;
    }
}
