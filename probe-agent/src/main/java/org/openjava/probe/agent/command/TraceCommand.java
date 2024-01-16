package org.openjava.probe.agent.command;

import org.openjava.probe.agent.asm.ProbeCallback;
import org.openjava.probe.agent.asm.ProbeMethodContext;
import org.openjava.probe.agent.asm.TraceMethodCallback;
import org.openjava.probe.agent.context.Context;
import org.openjava.probe.agent.data.TraceAdviceParam;
import org.openjava.probe.agent.server.ProbeAgentServer;
import org.openjava.probe.agent.session.Session;
import org.openjava.probe.agent.session.SessionState;
import org.openjava.probe.agent.transformer.ClassTransformerManager;
import org.openjava.probe.shared.message.Message;

import java.util.ArrayList;
import java.util.List;

public class TraceCommand extends ProbeCommand<TraceCommand.TraceParam> {

    public TraceCommand(String[] params) throws Exception {
        super(params);
    }

    @Override
    public void execute(Context context) {
        Session session = context.session();
        if (session.compareAndSet(SessionState.IDLE, SessionState.BUSY)) {
            ClassTransformerManager transformerManager = ProbeAgentServer.getInstance().transformerManager();
            ProbeCallback callback = new TraceMethodCallback(session, TraceAdviceParam.of(param.maxTimes));
            ProbeMethodContext probeContext = ProbeMethodContext.of(param.traceMethods, callback);
            transformerManager.enhance(context.instrumentation(), param.className, param.methodName, probeContext);

            if (probeContext.matchedMethods() > 0) {
                session.write(Message.info(String.format("%s methods traced in %s class",
                    probeContext.matchedMethods(), probeContext.matchedClass().getSimpleName())));
                session.synchronize();
                session.addCachedClass(probeContext.matchedClass());
            } else {
                session.setState(SessionState.IDLE);
                session.write(Message.error("No methods enhanced"));
            }
        } else {
            session.write(Message.error("Illegal user session state: " + session.getState()));
        }
    }

    @Override
    public Class<TraceParam> paramClass() {
        return TraceParam.class;
    }

    public static class TraceParam extends ProbeParam {
        private String className;
        private String methodName;
        private List<String> traceMethods;
        private Integer maxTimes;

        public TraceParam(String[] params) {
            super(params);
        }

        @Override
        public void parseParams(String[] params) {
            if (params.length < 2) {
                throw new IllegalArgumentException("Miss trace command params");
            }

            this.className = params[0];
            String[] methods = params[1].split(",");
            this.methodName = methods[0];

            this.traceMethods = new ArrayList<>();
            for (int i = 1; i < methods.length; i++) {
                this.traceMethods.add(methods[i]);
            }

            for (int i = 2; i < params.length; i++) {
                String param = params[i];
                if (param.startsWith("-p")) {
                    param = param.substring(2);
                    int index = param.indexOf('=');
                    if (index <= 0 || index >= param.length() - 1) {
                        throw new IllegalArgumentException("Illegal trace command params");
                    }
                    String key = param.substring(0, index);
                    String value = param.substring(index + 1);
                    if ("maxTimes".equalsIgnoreCase(key)) {
                        try {
                            maxTimes = Integer.parseInt(value);
                        } catch (Exception ex) {
                            throw new IllegalArgumentException("Illegal trace maxTime params");
                        }
                    } else {
                        throw new IllegalArgumentException("unknown trace params: " + key);
                    }
                } else {
                    throw new IllegalArgumentException("Illegal trace command params");
                }
            }
        }
    }
}
