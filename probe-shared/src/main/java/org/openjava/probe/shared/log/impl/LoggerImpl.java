package org.openjava.probe.shared.log.impl;

import org.openjava.probe.shared.log.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LoggerImpl implements Logger {
    private final String name;

    private final LoggerImpl parent;

    private List<LoggerImpl> children;

    private final LoggerContext context;

    private ListAppender<LoggingEvent> listAppender;

    private Level level;

    private Level effectiveLevel;

    private boolean additive = true;

    LoggerImpl(String name, LoggerImpl parent, LoggerContext context) {
        this.name = name;
        this.parent = parent;
        this.context = context;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public synchronized void setLevel(Level newLevel) {
        if (this.level != newLevel) {
            if (newLevel == null && this.parent == null) {
                throw new IllegalArgumentException("The level of the root logger cannot be set to null");
            } else {
                this.level = newLevel;
                if (newLevel == null) {
                    this.effectiveLevel = this.parent.getEffectiveLevel();
                } else {
                    this.effectiveLevel = newLevel;
                }

                if (this.children != null) {
                    for (LoggerImpl child : children) {
                        child.handleParentLevelChange(this.effectiveLevel);
                    }
                }
            }
        }
    }

    @Override
    public Level getEffectiveLevel() {
        return this.effectiveLevel;
    }

    @Override
    public void setAdditive(boolean additive) {
        this.additive = additive;
    }

    @Override
    public Logger getChildByName(String childName) {
        if (children == null) {
            return null;
        }

        for (final Logger childLoggerI : this.children) {
            final String childNameI = childLoggerI.getName();

            if (childName.equals(childNameI)) {
                return childLoggerI;
            }
        }
        // no child found
        return null;
    }

    @Override
    public Logger createChildByName(String childName) {
        int indexI = name.indexOf('.', this.name.length() + 1);
        if (indexI != -1) {
            throw new IllegalArgumentException("For logger [" + this.name + "] child name [" + childName
                + " passed as parameter, may not include '.' after index" + (this.name.length() + 1));
        }

        if (children == null) {
            children = new CopyOnWriteArrayList<>();
        }

        LoggerImpl childLogger = new LoggerImpl(childName, this, this.context);
        children.add(childLogger);
        childLogger.effectiveLevel = this.effectiveLevel;

        return childLogger;
    }

    @Override
    public synchronized void addAppender(Appender<LoggingEvent> appender) {
        if (this.listAppender == null) {
            this.listAppender = new ListAppenderImpl<>();
        }

        this.listAppender.addAppender(appender);
    }

    @Override
    public boolean isTraceEnabled() {
        return this.effectiveLevel.getCode() <= Level.TRACE.getCode();
    }

    @Override
    public void trace(String message) {
        append(Level.TRACE, message, null, null);
    }

    @Override
    public void trace(String message, Object param) {
        append(Level.TRACE, message, new Object[] {param}, null);
    }

    @Override
    public void trace(String message, Object param1, Object param2) {
        append(Level.TRACE, message, new Object[] {param1, param2}, null);
    }

    @Override
    public void trace(String message, Object... params) {
        append(Level.TRACE, message, params, null);
    }

    @Override
    public void trace(String message, Throwable ex) {
        append(Level.TRACE, message, null, ex);
    }

    @Override
    public boolean isDebugEnabled() {
        return this.effectiveLevel.getCode() <= Level.DEBUG.getCode();
    }

    @Override
    public void debug(String message) {
        append(Level.DEBUG, message, null, null);
    }

    @Override
    public void debug(String message, Object param) {
        append(Level.DEBUG, message, new Object[] {param}, null);
    }

    @Override
    public void debug(String message, Object param1, Object param2) {
        append(Level.DEBUG, message, new Object[] {param1, param2}, null);
    }

    @Override
    public void debug(String message, Object... params) {
        append(Level.DEBUG, message, params, null);
    }

    @Override
    public void debug(String message, Throwable ex) {
        append(Level.DEBUG, message, null, ex);
    }

    @Override
    public boolean isInfoEnabled() {
        return this.effectiveLevel.getCode() <= Level.INFO.getCode();
    }

    @Override
    public void info(String message) {
        append(Level.INFO, message, null, null);
    }

    @Override
    public void info(String message, Object param) {
        append(Level.INFO, message, new Object[] {param}, null);
    }

    @Override
    public void info(String message, Object param1, Object param2) {
        append(Level.INFO, message, new Object[] {param1, param2}, null);
    }

    @Override
    public void info(String message, Object... params) {
        append(Level.INFO, message, params, null);
    }

    @Override
    public void info(String message, Throwable ex) {
        append(Level.INFO, message, null, ex);
    }

    @Override
    public boolean isWarnEnabled() {
        return this.effectiveLevel.getCode() <= Level.WARN.getCode();
    }

    @Override
    public void warn(String message) {
        append(Level.WARN, message, null, null);
    }

    @Override
    public void warn(String message, Object param) {
        append(Level.WARN, message, new Object[] {param}, null);
    }

    @Override
    public void warn(String message, Object param1, Object param2) {
        append(Level.WARN, message, new Object[] {param1, param2}, null);
    }

    @Override
    public void warn(String message, Object... params) {
        append(Level.WARN, message, params, null);
    }

    @Override
    public void warn(String message, Throwable ex) {
        append(Level.WARN, message, null, ex);
    }

    @Override
    public boolean isErrorEnabled() {
        return this.effectiveLevel.getCode() <= Level.ERROR.getCode();
    }

    @Override
    public void error(String message) {
        append(Level.ERROR, message, null, null);
    }

    @Override
    public void error(String message, Object param) {
        append(Level.ERROR, message, new Object[] {param}, null);
    }

    @Override
    public void error(String message, Object param1, Object param2) {
        append(Level.ERROR, message, new Object[] {param1, param2}, null);
    }

    @Override
    public void error(String message, Object... params) {
        append(Level.ERROR, message, params, null);
    }

    @Override
    public void error(String message, Throwable ex) {
        append(Level.ERROR, message, null, ex);
    }

    private synchronized void handleParentLevelChange(Level newEffectiveLevel) {
        if (this.level == null) {
            this.effectiveLevel = newEffectiveLevel;
            if (this.children != null) {
                for (LoggerImpl child : children) {
                    child.handleParentLevelChange(newEffectiveLevel);
                }
            }
        }
    }

    private void append(Level level, String message, Object[] params, Throwable ex) {
        if (this.effectiveLevel.getCode() > level.getCode()) {
            return;
        }

        LoggingEvent event = LoggingEvent.of(this, level, message, params, ex);
        for(LoggerImpl l = this; l != null; l = l.parent) {
            if (l.listAppender != null) {
                l.listAppender.append(event);
            }

            if (!l.additive) {
                break;
            }
        }
    }
}
