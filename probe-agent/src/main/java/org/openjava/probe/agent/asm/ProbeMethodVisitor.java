package org.openjava.probe.agent.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;
import org.openjava.probe.core.api.ProbeMethod;
import org.openjava.probe.core.api.ProbeMethodAPI;
import org.openjava.probe.shared.util.ObjectUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ProbeMethodVisitor extends AdviceAdapter implements Opcodes {

    private static final String PROBE_ANNOTATION_DESC = Type.getType(ProbeMethod.class).getDescriptor();

    private static final Type PROBE_API_TYPE = Type.getType(ProbeMethodAPI.class);

    private static final Type THROWABLE_TYPE = Type.getObjectType("java/lang/Throwable");

    private static final Method ENTER_METHOD = new Method("enterMethod", "(I[Ljava/lang/Object;)V");

    private static final Method EXIT_METHOD = new Method("exitMethod", "(I[Ljava/lang/Object;Ljava/lang/Object;)V");

    private static final Method EXIT_EXCEPTION_METHOD = new Method("exitMethodOnException", "(I[Ljava/lang/Object;Ljava/lang/Throwable;)V");

    private static final Method BEFORE_INVOKE_METHOD = new Method("beforeInvoke", "(ILjava/lang/String;Ljava/lang/String;)V");

    private static final Method AFTER_INVOKE_METHOD = new Method("afterInvoke", "(ILjava/lang/String;Ljava/lang/String;)V");

    private static final AtomicInteger PROBE_ID = new AtomicInteger(0);

    private int probeId = 0;
    private int paramsLocal = 0;
    private boolean needProbe = false;
    private final ProbeMethodContext context;
    private final List<String> traceMethods;

    public ProbeMethodVisitor(int api, MethodVisitor mv, int access, String name, String descriptor, ProbeMethodContext context) {
        super(api, mv, access, name, descriptor);
        this.context = context;
        this.traceMethods = context.traceMethods();
    }

    public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
        AnnotationVisitor av = super.visitAnnotation(descriptor, visible);
        if (av != null && ObjectUtils.equals(PROBE_ANNOTATION_DESC, descriptor)) {
            av = new ProbeAnnotationVisitor(api, av);
        }
        return av;
    }

    @Override
    public void visitCode() {
        // if no method body like abstract method, never happened then
        if (probeId <= 0) {
            probeId = PROBE_ID.incrementAndGet();
            AnnotationVisitor av = super.visitAnnotation(PROBE_ANNOTATION_DESC, true);
            av.visit(ProbeMethod.PROBE_ID, probeId);
            av.visitEnd();
            needProbe = true;
        }
        super.visitCode();
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
        if (traceMethods != null && traceMethods.contains(name)) {
            // try catch statement supported
//            Label label0 = new Label();
//            Label label1 = new Label();
//            Label label2 = new Label();
//            visitTryCatchBlock(label0, label1, label2, null);
//            visitLabel(label0);

            push(probeId);
            visitLdcInsn(owner);
            visitLdcInsn(name);
            invokeStatic(PROBE_API_TYPE, BEFORE_INVOKE_METHOD);
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
//            visitLabel(label1);
            push(probeId);
            visitLdcInsn(owner);
            visitLdcInsn(name);
            invokeStatic(PROBE_API_TYPE, AFTER_INVOKE_METHOD);
            context.incMatchedMethods();
//            int exceptionLocal = newLocal(THROWABLE_TYPE);
//            Label label3 = new Label();
//            visitJumpInsn(GOTO, label3);
//            visitLabel(label2);
//            visitVarInsn(ASTORE, exceptionLocal);
//
//            push(probeId);
//            visitLdcInsn(owner);
//            visitLdcInsn(name);
//            invokeStatic(PROBE_API_TYPE, AFTER_INVOKE_METHOD);
//
//            visitVarInsn(ALOAD, exceptionLocal);
//            visitInsn(ATHROW);
//            visitLabel(label3);
        } else {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        // if no method body like abstract method, probeId = 0 then
        if (probeId > 0) {
            context.onMethodProbe(probeId, getName(), methodDesc);
            context.incMatchedMethods();
        }

        probeId = 0;
        paramsLocal = 0;
        needProbe = false;
    }

    protected void onMethodEnter() {
        if (needProbe) {
            paramsLocal = newLocal(Type.getType(Object[].class));
            loadArgArray();
            storeLocal(paramsLocal);
            push(probeId);
            loadLocal(paramsLocal);
            invokeStatic(PROBE_API_TYPE, ENTER_METHOD);
        }
    }

    protected void onMethodExit(final int opcode) {
        if (needProbe) {
            if (opcode == RETURN) {
                push(probeId);
                loadLocal(paramsLocal);
                visitInsn(ACONST_NULL);
                invokeStatic(PROBE_API_TYPE, EXIT_METHOD);
            } else if (opcode == ARETURN) {
                int returnLocal = newLocal(getReturnType());
                dup();
                storeLocal(returnLocal);
                push(probeId);
                loadLocal(paramsLocal);
                loadLocal(returnLocal);
                invokeStatic(PROBE_API_TYPE, EXIT_METHOD);
            } else if (opcode == ATHROW) {
                int exceptionLocal = newLocal(THROWABLE_TYPE);
                dup();
                storeLocal(exceptionLocal);
                push(probeId);
                loadLocal(paramsLocal);
                loadLocal(exceptionLocal);
                invokeStatic(PROBE_API_TYPE, EXIT_EXCEPTION_METHOD);
            } else if (opcode == LRETURN || opcode == DRETURN) {
                int returnLocal = newLocal(getReturnType());
                dup2();
                box(getReturnType());
                storeLocal(returnLocal);
                push(probeId);
                loadLocal(paramsLocal);
                loadLocal(returnLocal);
                invokeStatic(PROBE_API_TYPE, EXIT_METHOD);
            } else {
                int returnLocal = newLocal(getReturnType());
                dup();
                box(getReturnType());
                storeLocal(returnLocal);
                push(probeId);
                loadLocal(paramsLocal);
                loadLocal(returnLocal);
                invokeStatic(PROBE_API_TYPE, EXIT_METHOD);
            }
        }
    }

    private class ProbeAnnotationVisitor extends AnnotationVisitor {
        public ProbeAnnotationVisitor(int api, AnnotationVisitor annotationVisitor) {
            super(api, annotationVisitor);
        }

        public void visit(final String name, final Object value) {
            super.visit(name, value);
            if (ProbeMethod.PROBE_ID.equals(name)) {
                ProbeMethodVisitor.this.probeId = (int)value;
            }
        }
    }
}
