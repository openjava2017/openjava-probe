package org.openjava.probe.agent.command;

import org.openjava.probe.agent.asm.MethodProbeCallback;
import org.openjava.probe.agent.context.Context;
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
            MethodProbeCallback callback = new MethodProbeCallback(session);
            transformerManager.enhance(context.instrumentation(), param.className, param.methodName, callback);
            if (callback.matchedMethods() > 0) {
                session.synchronize();
                session.write(Message.info(String.format("%s classes matched, %s methods enhanced",
                    callback.matchedClasses(), callback.matchedMethods())));
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

    static class MonitorParam extends ProbeCommand.ProbeParam {
        private String className;
        private String methodName;

        public MonitorParam(String[] params) {
            super(params);
        }

        @Override
        public void parseParams(String[] params) {
            if (params.length < 2) {
                throw new IllegalArgumentException("Miss params for monitor command");
            }

            this.className = params[0];
            this.methodName = params[1];
        }
    }
}
