package org.openjava.probe.shared.message;

public enum MessageHeader {
    // Agent Server -> Client
    INFO_MESSAGE(0),
    // Client -> Agent Server
    USER_COMMAND(1),
    // Agent Server -> Client
    SESSION_STATE(2),
    // Agent Server -> Client
    DUMP_CLASS(3);

    private int code;

    MessageHeader(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public boolean equalTo(int code) {
        return this.getCode() == code;
    }
}
