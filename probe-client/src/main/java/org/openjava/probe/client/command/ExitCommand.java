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
        if (param != null) {
            System.out.println(param);
        }

        if(session.compareAndSet(SessionState.BUSY, SessionState.IDLE)) {
            synchronized (session) {
                session.notify();
            }
        }
    }
}
