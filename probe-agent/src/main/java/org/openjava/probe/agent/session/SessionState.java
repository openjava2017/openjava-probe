package org.openjava.probe.agent.session;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public enum SessionState {
    IDLE(0),

    BUSY(1),

    CLOSED(2);

    private int code;

    SessionState(int code) {
        this.code = code;
    }

    public static Optional<SessionState> of(int code) {
        Stream<SessionState> states = Arrays.stream(SessionState.values());
        return states.filter(state -> state.code == code).findFirst();
    }

    public boolean equalTo(int code) {
        return this.code == code;
    }

    public int code() {
        return this.code;
    }
}
