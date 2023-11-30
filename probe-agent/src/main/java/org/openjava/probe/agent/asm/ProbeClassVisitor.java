package org.openjava.probe.agent.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.openjava.probe.shared.util.Matcher;

import java.lang.reflect.Modifier;

public class ProbeClassVisitor extends ClassVisitor {

    private final Class clazz;
    private final Matcher<String> methodMatcher;
    private final ProbeCallback callback;

    public ProbeClassVisitor(Class clazz, int api, ClassVisitor cv, Matcher<String> methodMatcher, ProbeCallback callback) {
        super(api, cv);
        this.clazz = clazz;
        this.methodMatcher = methodMatcher;
        this.callback = callback;
    }

    public MethodVisitor visitMethod(final int access, final String name, final String descriptor, final String signature, final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

        if (mv != null && methodMatcher.match(name) && !Modifier.isAbstract(access) && !Modifier.isNative(access)) {
            mv = new ProbeMethodVisitor(clazz, api, mv, access, name, descriptor, callback);
        }

        return mv;
    }
}
