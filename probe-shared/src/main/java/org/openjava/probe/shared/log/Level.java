package org.openjava.probe.shared.log;

public enum Level {
    TRACE(1, "TRACE"),
    DEBUG(2, "DEBUG"),
    INFO(3, "INFO"),
    WARN(4, "WARN"),
    ERROR(5, "ERROR");

    private int code;

    private String name;

    Level(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public boolean equalTo(int code) {
        return this.getCode() == code;
    }
}
