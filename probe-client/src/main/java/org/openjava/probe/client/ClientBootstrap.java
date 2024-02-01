package org.openjava.probe.client;

import org.openjava.probe.client.context.UserEnvironment;
import org.openjava.probe.client.gui.ProbeDashboard;
import org.openjava.probe.shared.log.Level;
import org.openjava.probe.shared.log.Logger;
import org.openjava.probe.shared.log.LoggerFactory;
import org.openjava.probe.shared.log.LoggingEvent;
import org.openjava.probe.shared.log.impl.ConsoleAppender;
import org.openjava.probe.shared.log.impl.FileAppender;
import org.openjava.probe.shared.log.impl.OutputStreamAppender;
import org.openjava.probe.shared.log.impl.SimplePatternLayout;

import java.io.File;
import java.security.CodeSource;

public class ClientBootstrap {
    public static void main(String[] args) throws Exception {
        // init log system
        CodeSource codeSource = ClientBootstrap.class.getProtectionDomain().getCodeSource();
        File jarFile = new File(codeSource.getLocation().toURI());
        File logFile = new File(jarFile.getParentFile(), "agent-client.log");
        OutputStreamAppender<LoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setLayout(new SimplePatternLayout());
        consoleAppender.start();

        OutputStreamAppender<LoggingEvent> fileAppender = new FileAppender<>(logFile, true);
        fileAppender.setLayout(new SimplePatternLayout());
        fileAppender.start();

        Logger logger = LoggerFactory.getLogger("org.openjava.probe.shared");
        logger.setLevel(Level.INFO);

        logger = LoggerFactory.getLogger("org.openjava.probe");
        logger.setLevel(Level.DEBUG);
        logger.addAppender(fileAppender);

        Logger root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.DEBUG);
        root.addAppender(consoleAppender);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                fileAppender.stop();
            } catch (Exception ex) {
                // Ignore it
            }
        }));
        // init log system end

        new ProbeDashboard(new UserEnvironment(String.join(",", args))).showDashboard();
    }
}
