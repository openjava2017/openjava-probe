package org.openjava.probe.shared.log;

public class LoggingEvent {
    private final Logger source;
    private final Level level;
    private final String message;
    private final Object[] params;
    private final Throwable exception;

    public static LoggingEvent of(Logger source, Level level, String message, Object[] params, Throwable exception) {
        return new LoggingEvent(source, level, message, params, exception);
    }

    private LoggingEvent(Logger source, Level level, String message, Object[] params, Throwable exception) {
        this.source = source;
        this.level = level;
        this.message = message;
        this.params = params;
        this.exception = exception;
    }

    public Logger getSource() {
        return source;
    }

    public Level getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public Object[] getParams() {
        return params;
    }

    public Throwable getException() {
        return exception;
    }
}
