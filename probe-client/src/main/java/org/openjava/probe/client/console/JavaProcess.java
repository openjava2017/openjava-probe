package org.openjava.probe.client.console;

import org.openjava.probe.shared.util.ObjectUtils;

public class JavaProcess {
    private String processId;
    private String mainClass;

    private JavaProcess(String processId, String mainClass) {
        this.processId = processId;
        this.mainClass = mainClass;
    }

    public static JavaProcess of(String processId, String mainClass) {
        return new JavaProcess(processId, mainClass);
    }

    public String processId() {
        return this.processId;
    }

    public String mainClass() {
        return this.mainClass;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof JavaProcess && ObjectUtils.equals(this.processId, ((JavaProcess) other).processId));
    }

    @Override
    public int hashCode() {
        return this.processId.hashCode();
    }

    public String toString() {
        return String.format("%s - %s", processId(), mainClass());
    }
}
