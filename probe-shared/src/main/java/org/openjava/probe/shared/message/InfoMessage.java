package org.openjava.probe.shared.message;

public class InfoMessage {
    public static final int INFO_LEVEL = 0;
    public static final int ERROR_LEVEL = 1;

    private int level;
    private String information;

    public InfoMessage(int level, String information) {
        this.level = level;
        this.information = information;
    }

    public int level() {
        return this.level;
    }

    public String information() {
        return this.information;
    }
}
