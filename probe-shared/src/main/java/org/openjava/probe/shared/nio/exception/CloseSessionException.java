package org.openjava.probe.shared.nio.exception;

public class CloseSessionException extends NioSessionException {
    public CloseSessionException(String message) {
        super(message);
    }

    public CloseSessionException(int code, String message) {
        super(code, message);
    }

    public CloseSessionException(String message, Throwable ex) {
        super(message, ex);
    }
}