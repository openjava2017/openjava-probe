package org.openjava.probe.shared.message;

public class DumpClass {
    private final String name;
    private final byte[] classBytes;

    private DumpClass(String name, byte[] classBytes) {
        this.name = name;
        this.classBytes = classBytes;
    }

    public static DumpClass of(String name, byte[] classBytes) {
        return new DumpClass(name, classBytes);
    }

    public String name() {
        return this.name;
    }

    public byte[] classBytes() {
        return this.classBytes;
    }
}
