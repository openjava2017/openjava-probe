package org.openjava.probe.agent.transformer;

import org.openjava.probe.agent.asm.ProbeCallback;
import org.openjava.probe.shared.log.Logger;
import org.openjava.probe.shared.log.LoggerFactory;
import org.openjava.probe.shared.util.Matcher;
import org.openjava.probe.shared.util.NameFullMatcher;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClassTransformerManager implements ClassFileTransformer {
    private static final Logger LOG = LoggerFactory.getLogger(ClassTransformerManager.class);

    private final List<ClassFileTransformer> transformers = new CopyOnWriteArrayList<>();

    public byte[] transform(ClassLoader loader, String className, Class<?> classRedefined, ProtectionDomain domain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        for (int i = 0; i < transformers.size(); i++) {
            byte[] classBytes = transformers.get(i).transform(loader, className, classRedefined, domain, classfileBuffer);
            if (classBytes != null) {
                classfileBuffer = classBytes;
            }
        }
        return classfileBuffer;
    }

    public void addClassFileTransformer(ClassFileTransformer transformer) {
        this.transformers.add(transformer);
    }

    public void removeClassFileTransformer(ClassFileTransformer transformer) {
        this.transformers.remove(transformer);
    }

    public void clearClassFileTransformers() {
        this.transformers.clear();
    }

    public void enhance(Instrumentation instrumentation, String className, String methodName, ProbeCallback callback) {
        Class matchedClass = null;
        Matcher<String> matcher = new NameFullMatcher(className);
        Class[] allClasses = instrumentation.getAllLoadedClasses();
        for (int i = 0; i < allClasses.length; i++) {
            if (matcher.match(allClasses[i].getName()) && enhanceAllowed(allClasses[i])) {
                matchedClass = allClasses[i];
                break;
            }
        }

        if (matchedClass != null) {
            ClassFileTransformer transformer = null;
            try {
                NameFullMatcher methodMatcher = new NameFullMatcher(methodName);
                transformer = new ProbeClassFileTransformer(matchedClass, methodMatcher, callback);
                transformers.add(transformer);
                instrumentation.retransformClasses(matchedClass);
            } catch (Throwable ex) {
                LOG.warn("retransform class {} failed.", matchedClass, ex);
            } finally {
                if (transformers != null) {
                    transformers.remove(transformer);
                }
            }
        }
    }

    private boolean enhanceAllowed(Class clazz) {
        if (clazz.isAnnotation() || clazz.isArray() || clazz.isInterface() || clazz.isEnum() ||
            clazz.isAnonymousClass() || clazz.isPrimitive()) {
            return false;
        }
        // for safety reason: System Class, Probe Class not allowed to be enhanced
        ClassLoader classLoader = clazz.getClassLoader();
        if (classLoader == null || classLoader == ClassLoader.getPlatformClassLoader() ||
            classLoader == ClassLoader.getSystemClassLoader() || classLoader == getClass().getClassLoader()) {
            return false;
        }
        return true;
    }
}
