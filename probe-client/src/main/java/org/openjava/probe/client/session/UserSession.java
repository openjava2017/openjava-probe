package org.openjava.probe.client.session;

import org.openjava.probe.shared.message.Message;
import org.openjava.probe.shared.nio.session.INioSession;

import java.util.concurrent.atomic.AtomicReference;

public class UserSession extends SessionOutputAdapter implements Session {
    private final AtomicReference<SessionState> state;

    public UserSession(INioSession session) {
        super(session);
        this.state = new AtomicReference<>(SessionState.IDLE);
    }

    @Override
    public long id() {
        return session.getId();
    }

    @Override
    public void write(Message message) {
        if (compareAndSet(SessionState.IDLE, SessionState.BUSY)) {
            super.write(message);
        } else {
            System.err.println("Cannot send the user command: user session is busy or closed");
        }
    }

    @Override
    public boolean requireIdle() throws InterruptedException {
        if (this.state.get() == SessionState.BUSY) {
            synchronized (this) {
                this.wait();
            }
        }
        return this.state.get() == SessionState.IDLE;
    }

    @Override
    public boolean compareAndSet(SessionState expectedState, SessionState newState) {
        return this.state.compareAndSet(expectedState, newState);
    }

    @Override
    public SessionState setState(SessionState state) {
        return this.state.getAndSet(state);
    }

    @Override
    public void destroy() {
        switch(setState(SessionState.CLOSED)) {
            case BUSY:
            case IDLE:
                session.destroy();
                break;
            case CLOSED:
                break;
        }
    }
}
