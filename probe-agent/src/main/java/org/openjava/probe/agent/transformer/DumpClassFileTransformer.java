package org.openjava.probe.agent.transformer;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class DumpClassFileTransformer implements ClassFileTransformer {

    private final Class clazz;
    private byte[] classBytes;

    public DumpClassFileTransformer(Class clazz) {
        this.clazz = clazz;
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classRedefined, ProtectionDomain domain, byte[] classBytes) {
        if (clazz == classRedefined) {
            this.classBytes = classBytes;
            return null;
        }

        return null;
    }

    public byte[] classBytes() {
        return this.classBytes;
    }
}
