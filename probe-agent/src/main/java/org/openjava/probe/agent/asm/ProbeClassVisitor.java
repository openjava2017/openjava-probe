package org.openjava.probe.agent.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.openjava.probe.shared.util.Matcher;

import java.lang.reflect.Modifier;

public class ProbeClassVisitor extends ClassVisitor {

    private final Class clazz;
    private final Matcher<String> methodMatcher;
    private final ProbeMethodContext context;

    public ProbeClassVisitor(Class clazz, int api, ClassVisitor cv, Matcher<String> methodMatcher, ProbeMethodContext context) {
        super(api, cv);
        this.clazz = clazz;
        this.methodMatcher = methodMatcher;
        this.context = context;
    }

    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        if (context != null) {
            context.onClassProbe(clazz);
        }

        super.visit(version, access, name, signature, superName, interfaces);
    }

    public MethodVisitor visitMethod(final int access, final String name, final String descriptor, final String signature, final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

        if (mv != null && methodMatcher.match(name) && !Modifier.isAbstract(access) && !Modifier.isNative(access)) {
            mv = new ProbeMethodVisitor(api, mv, access, name, descriptor, context);
        }

        return mv;
    }
}
