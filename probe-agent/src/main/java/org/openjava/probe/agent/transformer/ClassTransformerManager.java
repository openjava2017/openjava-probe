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
        for (ClassFileTransformer transformer : transformers) {
            byte[] classBytes = transformer.transform(loader, className, classRedefined, domain, classfileBuffer);
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
        Class<?> matchedClass = null;
        Matcher<String> matcher = new NameFullMatcher(className);
        Class<?>[] allClasses = instrumentation.getAllLoadedClasses();
        for (Class<?> allClass : allClasses) {
            if (matcher.match(allClass.getName()) && enhanceAllowed(allClass)) {
                matchedClass = allClass;
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
                ex.printStackTrace();
                LOG.warn("re-transform class {} failed.", matchedClass, ex);
            } finally {
                if (transformer != null) {
                    transformers.remove(transformer);
                }
            }
        }
    }

    private boolean enhanceAllowed(Class<?> clazz) {
        if (clazz.isAnnotation() || clazz.isArray() || clazz.isInterface() || clazz.isEnum() ||
            clazz.isAnonymousClass() || clazz.isPrimitive()) {
            return false;
        }

        // for safety reason: System Class, Probe Class not allowed to be enhanced
        ClassLoader classLoader = clazz.getClassLoader();
        return classLoader != null && classLoader != ClassLoader.getSystemClassLoader().getParent() &&
            classLoader != getClass().getClassLoader();
    }
}
