package org.openjava.probe.shared.nio.exception;

public class OpenSessionException extends NioSessionException {
    public OpenSessionException(String message) {
        super(message);
    }

    public OpenSessionException(int code, String message) {
        super(code, message);
    }

    public OpenSessionException(String message, Throwable ex) {
        super(message, ex);
    }
}