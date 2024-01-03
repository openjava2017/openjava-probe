package org.openjava.probe.shared.message;

public enum MessageHeader {
    // Agent Server -> Client
    INFO_MESSAGE((short)0, (short)0),
    // Client -> Agent Server: monitor className method
    USER_COMMAND((short)1, (short)0),
    // Agent Server -> Client
    SESSION_STATE((short)2, (short)0);

    private short type;
    private short command;

    MessageHeader(short type, short command) {
        this.type = type;
        this.command = command;
    }

    public int getCode() {
        int code = type;
        return (code << 16) | (command << 16 >>> 16); // remove signed bit
    }

    public boolean equalTo(int code) {
//        (short)(code >>> 16)
//        (short)(code & 0x0000ffff)
        return this.getCode() == code;
    }
}
