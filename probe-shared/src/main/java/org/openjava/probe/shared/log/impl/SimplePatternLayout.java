package org.openjava.probe.shared.log.impl;

import org.openjava.probe.shared.log.Layout;
import org.openjava.probe.shared.log.LoggingEvent;
import org.openjava.probe.shared.util.DateUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class SimplePatternLayout implements Layout<LoggingEvent> {
    @Override
    public String layout(LoggingEvent event) {
        StringWriter sw = new StringWriter();
        String message = LayoutHelper.arrayFormat(event.getMessage(), event.getParams());
        sw.append(String.format("%s %s [%s] - %s\n", DateUtils.formatNow(), event.getLevel().getName(), event.getSource().getName(), message));

        if (event.getException() != null) {
            PrintWriter pw = new PrintWriter(sw);
            event.getException().printStackTrace(pw);
        }

        return sw.toString();
    }
}
