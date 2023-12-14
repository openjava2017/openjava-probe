package org.openjava.probe.shared.nio.session;

public class SessionContext {
    private NioSession session;
    private ISessionEventListener listener;

    private SessionContext(NioSession session, ISessionEventListener listener) {
        this.session = session;
        this.listener = listener;
    }

    public INioSession session() {
        return this.session;
    }

    public void fireSessionCreated() {
        if (listener != null) {
            listener.onSessionCreated(session);
        }
    }

    public void fireSessionClosed() {
        switch(session.state.getAndSet(SessionState.CLOSED)) {
            case CONNECTED:
            case CLOSING:
                if(listener != null) {
                    listener.onSessionClosed(session);
                }
                break;
            default:
                break;
        }
    }

    public static SessionContext create(NioSession session, ISessionEventListener listener) {
        return new SessionContext(session, listener);
    }
}
