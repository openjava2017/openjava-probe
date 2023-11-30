package org.openjava.probe.shared.log;

import org.openjava.probe.shared.util.DateUtils;

public class LoggerFactory {

    public static Logger getLogger(String name) {
        return new DummyLogger(name);
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    private static class DummyLogger implements Logger {
        private String name;

        public DummyLogger(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isTraceEnabled() {
            return true;
        }

        @Override
        public void trace(String message) {
            String str = String.format("%s TRACE [%s] - %s", DateUtils.formatNow(), name, message);
            System.out.println(str);
        }

        @Override
        public void trace(String message, Object param) {
            trace(message);
        }

        @Override
        public void trace(String message, Object param1, Object param2) {
            trace(message);
        }

        @Override
        public void trace(String message, Object... params) {
            trace(message);
        }

        @Override
        public void trace(String message, Throwable ex) {
            trace(message);
            ex.printStackTrace(System.out);
        }

        @Override
        public boolean isDebugEnabled() {
            return true;
        }

        @Override
        public void debug(String message) {
            String str = String.format("%s DEBUG [%s] - %s", DateUtils.formatNow(), name, message);
            System.out.println(str);
        }

        @Override
        public void debug(String message, Object param) {
            debug(message);
        }

        @Override
        public void debug(String message, Object param1, Object param2) {
            debug(message);
        }

        @Override
        public void debug(String message, Object... params) {
            debug(message);
        }

        @Override
        public void debug(String message, Throwable ex) {
            debug(message);
        }

        @Override
        public boolean isInfoEnabled() {
            return true;
        }

        @Override
        public void info(String message) {
            String str = String.format("%s INFO [%s] - %s", DateUtils.formatNow(), name, message);
            System.out.println(str);
        }

        @Override
        public void info(String message, Object param) {
            info(message);
        }

        @Override
        public void info(String message, Object param1, Object param2) {
            info(message);
        }

        @Override
        public void info(String message, Object... params) {
            info(message);
        }

        @Override
        public void info(String message, Throwable ex) {
            info(message);
        }

        @Override
        public boolean isWarnEnabled() {
            return true;
        }

        @Override
        public void warn(String message) {
            String str = String.format("%s WARN [%s] - %s", DateUtils.formatNow(), name, message);
            System.out.println(str);
        }

        @Override
        public void warn(String message, Object param) {
            warn(message);
        }

        @Override
        public void warn(String message, Object... params) {
            warn(message);
        }

        @Override
        public void warn(String message, Object param1, Object param2) {
            warn(message);
        }

        @Override
        public void warn(String message, Throwable ex) {
            warn(message);
        }

        @Override
        public boolean isErrorEnabled() {
            return true;
        }

        @Override
        public void error(String message) {
            String str = String.format("%s ERROR [%s] - %s", DateUtils.formatNow(), name, message);
            System.out.println(str);
        }

        @Override
        public void error(String message, Object param) {
            error(message);
        }

        @Override
        public void error(String message, Object param1, Object param2) {
            error(message);
        }

        @Override
        public void error(String message, Object... params) {
            error(message);
        }

        @Override
        public void error(String message, Throwable ex) {
            error(message);
        }
    }
}
