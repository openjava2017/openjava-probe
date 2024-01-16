package org.openjava.probe.agent.command;

import org.openjava.probe.agent.asm.MonitorMethodCallback;
import org.openjava.probe.agent.asm.ProbeCallback;
import org.openjava.probe.agent.asm.ProbeMethodContext;
import org.openjava.probe.agent.context.Context;
import org.openjava.probe.agent.data.MonitorAdviceParam;
import org.openjava.probe.agent.server.ProbeAgentServer;
import org.openjava.probe.agent.session.Session;
import org.openjava.probe.agent.session.SessionState;
import org.openjava.probe.agent.transformer.ClassTransformerManager;
import org.openjava.probe.shared.message.Message;

public class MonitorCommand extends ProbeCommand<MonitorCommand.MonitorParam> {

    public MonitorCommand(String[] params) throws Exception {
        super(params);
    }

    @Override
    public void execute(Context context) {
        Session session = context.session();
        if (session.compareAndSet(SessionState.IDLE, SessionState.BUSY)) {
            ClassTransformerManager transformerManager = ProbeAgentServer.getInstance().transformerManager();
            ProbeCallback callback = new MonitorMethodCallback(session, MonitorAdviceParam.of(param.maxTimes));
            ProbeMethodContext probeContext = ProbeMethodContext.of(callback);
            transformerManager.enhance(context.instrumentation(), param.className, param.methodName, probeContext);

            if (probeContext.matchedMethods() > 0) {
                session.write(Message.info(String.format("%s methods monitored in %s class",
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
    public Class<MonitorParam> paramClass() {
        return MonitorParam.class;
    }

    public static class MonitorParam extends ProbeCommand.ProbeParam {
        private String className;
        private String methodName;
        private Integer maxTimes;

        public MonitorParam(String[] params) {
            super(params);
        }

        @Override
        public void parseParams(String[] params) {
            if (params.length < 2) {
                throw new IllegalArgumentException("Miss monitor command params");
            }

            this.className = params[0];
            this.methodName = params[1];

            for (int i = 2; i < params.length; i++) {
                String param = params[i];
                if (param.startsWith("-p")) {
                    param = param.substring(2);
                    int index = param.indexOf('=');
                    if (index <= 0 || index >= param.length() - 1) {
                        throw new IllegalArgumentException("Illegal monitor command params");
                    }
                    String key = param.substring(0, index);
                    String value = param.substring(index + 1);
                    if ("maxTimes".equalsIgnoreCase(key)) {
                        try {
                            maxTimes = Integer.parseInt(value);
                        } catch (Exception ex) {
                            throw new IllegalArgumentException("Illegal monitor maxTime params");
                        }
                    } else {
                        throw new IllegalArgumentException("unknown monitor params: " + key);
                    }
                } else {
                    throw new IllegalArgumentException("Illegal monitor command params");
                }
            }
        }
    }
}
