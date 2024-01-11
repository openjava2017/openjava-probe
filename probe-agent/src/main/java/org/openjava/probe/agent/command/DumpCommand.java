package org.openjava.probe.agent.command;

import org.openjava.probe.agent.context.Context;
import org.openjava.probe.agent.server.ProbeAgentServer;
import org.openjava.probe.agent.session.Session;
import org.openjava.probe.agent.transformer.ClassTransformerManager;
import org.openjava.probe.agent.transformer.DumpClassFileTransformer;
import org.openjava.probe.shared.message.DumpClass;
import org.openjava.probe.shared.message.Message;
import org.openjava.probe.shared.message.MessageHeader;
import org.openjava.probe.shared.message.codec.ClassPayloadCodec;
import org.openjava.probe.shared.util.Matcher;
import org.openjava.probe.shared.util.NameFullMatcher;

import java.lang.instrument.Instrumentation;

public class DumpCommand extends ProbeCommand<DumpCommand.DumpParam> {

    public DumpCommand(String[] params) throws Exception {
        super(params);
    }

    @Override
    public void execute(Context context) {
        Class<?> matchedClass = null;
        Instrumentation instrumentation = context.instrumentation();
        Session session = context.session();
        Matcher<String> matcher = new NameFullMatcher(param.className);
        Class<?>[] allClasses = instrumentation.getAllLoadedClasses();
        for (Class<?> allClass : allClasses) {
            if (matcher.match(allClass.getName())) {
                matchedClass = allClass;
                break;
            }
        }

        if (matchedClass != null) {
            DumpClassFileTransformer transformer = new DumpClassFileTransformer(matchedClass);
            ClassTransformerManager transformerManager = ProbeAgentServer.getInstance().transformerManager();
            try {
                transformerManager.addClassFileTransformer(transformer);
                instrumentation.retransformClasses(matchedClass);
                byte[] classBytes = transformer.classBytes();
                if (classBytes != null && classBytes.length > 0) {
                    DumpClass clazz = DumpClass.of(matchedClass.getSimpleName(), classBytes);
                    session.write(Message.of(MessageHeader.DUMP_CLASS, clazz, ClassPayloadCodec.getEncoder()));
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
                session.write(Message.error(String.format("dump class %s failed.", param.className)));
            } finally {
                transformerManager.removeClassFileTransformer(transformer);
            }
        } else {
            session.write(Message.error(String.format("no class %s found.", param.className)));
        }
    }

    @Override
    public Class<DumpParam> paramClass() {
        return DumpParam.class;
    }

    public static class DumpParam extends ProbeParam {
        private String className;

        public DumpParam(String[] params) {
            super(params);
        }

        @Override
        public void parseParams(String[] params) {
            if (params.length < 1) {
                throw new IllegalArgumentException("Miss dump command params");
            }

            this.className = params[0];
        }
    }
}
