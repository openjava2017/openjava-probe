package org.openjava.probe.agent.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.openjava.probe.shared.util.Matcher;
import org.openjava.probe.shared.util.NameFullMatcher;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class TestProbeClassVisitor {
    public static void main(String[] args) throws Exception {
        InputStream is = new FileInputStream("/Users/huanggang/Work/Projects/openjava-probe/probe-agent/build/classes/java/main/org/openjava/probe/agent/asm/ProbeTestService.class");
        ClassReader reader = new ClassReader(is);

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        Matcher<String> methodMatcher = new NameFullMatcher("testTraceMethod");
        List<String> traceMethods = new ArrayList<>();
        traceMethods.add("println");
        ProbeClassVisitor cv = new ProbeClassVisitor(null, Opcodes.ASM9, cw, methodMatcher, null);
        // 如果MethodVisitor存在LocalVariablesSorter则，需使用EXPAND_FRAMES参数
        reader.accept(cv, ClassReader.EXPAND_FRAMES);
        byte[] packet = cw.toByteArray();
        OutputStream os = new FileOutputStream("/Users/huanggang/Desktop/ProbeTestService.class");
        os.write(packet);
        os.flush();
        os.close();
    }
}
