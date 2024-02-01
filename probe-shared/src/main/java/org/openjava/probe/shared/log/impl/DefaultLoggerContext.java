package org.openjava.probe.shared.log.impl;

import org.openjava.probe.shared.log.Level;
import org.openjava.probe.shared.log.Logger;
import org.openjava.probe.shared.log.LoggerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultLoggerContext implements LoggerContext {

    private final Logger root;

    private final Map<String, Logger> caches;

    public DefaultLoggerContext() {
        this.root = new LoggerImpl(Logger.ROOT_LOGGER_NAME, null, this);
        this.root.setLevel(Level.DEBUG);
        this.caches = new ConcurrentHashMap<>();
        this.caches.put(Logger.ROOT_LOGGER_NAME, this.root);
    }

    @Override
    public Logger getLogger(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name argument cannot be null");
        }

        if (Logger.ROOT_LOGGER_NAME.equalsIgnoreCase(name)) {
            return root;
        }

        int i = 0;
        Logger logger = root;

        // check if the desired logger exists, if it does, return it
        // without further ado.
        Logger childLogger = caches.get(name);
        // if we have the child, then let us return it without wasting time
        if (childLogger != null) {
            return childLogger;
        }

        // if the desired logger does not exist, them create all the loggers
        // in between as well (if they don't already exist)
        String childName;
        while (true) {
            int h = name.indexOf('.', i);
            if (h == -1) {
                childName = name;
            } else {
                childName = name.substring(0, h);
            }
            // move i left of the last point
            i = h + 1;
            synchronized (logger) {
                childLogger = logger.getChildByName(childName);
                if (childLogger == null) {
                    childLogger = logger.createChildByName(childName);
                    caches.put(childName, childLogger);
                }
            }
            logger = childLogger;
            if (h == -1) {
                return childLogger;
            }
        }
    }
}
