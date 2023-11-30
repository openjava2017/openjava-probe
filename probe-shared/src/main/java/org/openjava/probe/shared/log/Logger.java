package org.openjava.probe.shared.log;

public interface Logger {

    String getName();

    boolean isTraceEnabled();

    void trace(String message);

    void trace(String message, Object param);

    void trace(String message, Object param1, Object param2);

    void trace(String message, Object... params);

    void trace(String message, Throwable ex);

    boolean isDebugEnabled();

    void debug(String message);

    void debug(String message, Object param);

    void debug(String message, Object param1, Object param2);

    void debug(String message, Object... params);

    void debug(String message, Throwable ex);

    boolean isInfoEnabled();

    void info(String message);

    void info(String message, Object param);

    void info(String message, Object param1, Object param2);

    void info(String message, Object... params);

    void info(String message, Throwable ex);

    boolean isWarnEnabled();

    void warn(String message);

    void warn(String message, Object param);

    void warn(String message, Object... params);

    void warn(String message, Object param1, Object param2);

    void warn(String message, Throwable ex);

    boolean isErrorEnabled();

    void error(String message);

    void error(String message, Object param);

    void error(String message, Object param1, Object param2);

    void error(String message, Object... params);

    void error(String message, Throwable ex);
}
