package org.openjava.probe.client.command;

import org.openjava.probe.client.session.Session;
import org.openjava.probe.client.session.SessionState;

public class ExitCommand extends MessageCommand {
    public ExitCommand(byte[] payload) {
        super(payload);
    }

    @Override
    public void execute(Context context) {
        Session session = context.session();
        if (session.setState(SessionState.IDLE) == SessionState.BUSY) {
            //TODO: clean
        }
    }
}
