package org.openjava.probe.agent.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.openjava.probe.agent.asm.ProbeClassVisitor;
import org.openjava.probe.agent.asm.ProbeMethodContext;
import org.openjava.probe.core.api.ProbeMethodAPI;
import org.openjava.probe.shared.log.Logger;
import org.openjava.probe.shared.log.LoggerFactory;
import org.openjava.probe.shared.util.Matcher;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class ProbeClassFileTransformer implements ClassFileTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(ProbeClassFileTransformer.class);

    private final Class<?> clazz;
    private final Matcher<String> methodMatcher;
    private final ProbeMethodContext context;

    public ProbeClassFileTransformer(Class<?> clazz, Matcher<String> methodMatcher, ProbeMethodContext context) {
        this.clazz = clazz;
        this.methodMatcher = methodMatcher;
        this.context = context;
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classRedefined, ProtectionDomain domain, byte[] classBytes) {
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

        try {
            LOG.debug("transform class {} ...", className);
            ProbeClassVisitor cv = new ProbeClassVisitor(classRedefined, Opcodes.ASM9, cw, methodMatcher, context);
            reader.accept(cv, ClassReader.EXPAND_FRAMES);
            return cw.toByteArray();
        } catch (Exception ex) {
            LOG.error("transform class " + className + " failed", ex);
            return null;
        }
    }
}
