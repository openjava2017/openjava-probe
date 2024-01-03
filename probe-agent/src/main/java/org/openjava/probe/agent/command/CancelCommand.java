package org.openjava.probe.agent.command;

import org.openjava.probe.agent.context.Context;
import org.openjava.probe.agent.session.Session;
import org.openjava.probe.agent.session.SessionState;
import org.openjava.probe.shared.message.Message;

public class CancelCommand extends ProbeCommand<ProbeCommand.NoneParam> {
    public CancelCommand(String[] params) throws Exception {
        super(params);
    }

    @Override
    public void execute(Context context) {
        Session session = context.session();
        switch (session.setState(SessionState.IDLE)) {
            case BUSY:
                session.clearMethodAdvices();
                session.synchronize();
                break;
            case CLOSED:
                session.write(Message.error("Illegal user session state: " + session.getState()));
                break;
            default:
        }
    }

    @Override
    public Class<NoneParam> paramClass() {
        return NoneParam.class;
    }
}
