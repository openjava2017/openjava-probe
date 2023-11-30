package org.openjava.probe.agent.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.openjava.probe.agent.asm.ProbeCallback;
import org.openjava.probe.agent.asm.ProbeClassVisitor;
import org.openjava.probe.core.api.ProbeMethodAPI;
import org.openjava.probe.shared.log.Logger;
import org.openjava.probe.shared.log.LoggerFactory;
import org.openjava.probe.shared.util.Matcher;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class ProbeClassFileTransformer implements ClassFileTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(ProbeClassFileTransformer.class);

    private final Class clazz;
    private final Matcher methodMatcher;
    private final ProbeCallback callback;

    public ProbeClassFileTransformer(Class clazz, Matcher<String> methodMatcher, ProbeCallback callback) {
        this.clazz = clazz;
        this.methodMatcher = methodMatcher;
        this.callback = callback;
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classRedefined, ProtectionDomain domain,
                            byte[] classBytes) throws IllegalClassFormatException {
        if (clazz != classRedefined) {
            return null;
        }

        try {
            if (loader != null) {
                loader.loadClass(ProbeMethodAPI.class.getName());
            }
        } catch (Throwable ex) {
            LOG.error("The classloader can not load ProbeMethodAPI, ignore it. classloader: {}, className: {}",
                loader.getClass().getName(), className, ex);
            return null;
        }

        ClassReader reader = new ClassReader(classBytes);
        // 扩展ClassWriter，优先使用线程上下文类加载器，避免加载到项目类出现ClassNotFoundException
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES) {
            protected ClassLoader getClassLoader() {
                return loader;
            }
        };

        ProbeClassVisitor cv = new ProbeClassVisitor(classRedefined, Opcodes.ASM9, cw, methodMatcher, callback);
        reader.accept(cv, ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }
}
