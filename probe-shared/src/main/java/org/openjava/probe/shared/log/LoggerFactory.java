package org.openjava.probe.shared.log;

import org.openjava.probe.shared.log.impl.*;

import java.io.File;

public class LoggerFactory {

    private static volatile LoggerContext context;

    public static Logger getLogger(String name) {
        return getContext().getLogger(name);
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    private static LoggerContext getContext() {
        if (context == null) {
            synchronized (LoggerFactory.class) {
                if (context == null) {
                    context = new DefaultLoggerContext();
                }
            }
        }
        return context;
    }

    public static void main(String[] args) throws Exception {
        OutputStreamAppender<LoggingEvent> appender = new ConsoleAppender<>();
        appender.setLayout(new SimplePatternLayout());
        appender.start();
        Logger root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.DEBUG);
        root.addAppender(appender);

        appender = new FileAppender<>(new File("/Users/huanggang/Desktop/testlog.log"), true);
        appender.setLayout(new SimplePatternLayout());
        appender.setImmediateFlush(true);
        appender.start();
        Logger logger = LoggerFactory.getLogger("org.openjava.probe.shared");
        logger.setLevel(Level.INFO);
        logger.setAdditive(true);
        logger.addAppender(appender);

        logger = LoggerFactory.getLogger(LoggerFactory.class);
        logger.info("fuck me");
        logger.info("fuck u, {}", "asshole");
        logger.info("fuck u, {} and {}", "bastard", "asshole");
        logger.info("here we go", new Exception("fuck me"));
        logger.info("{}, {}, {}, {}", new Object[] {new int[] {0, 1}, "2", "3", new Object()});
        appender.stop();
    }
}
