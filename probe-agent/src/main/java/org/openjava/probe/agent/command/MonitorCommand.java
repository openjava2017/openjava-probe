package org.openjava.probe.agent.command;

import org.openjava.probe.agent.advice.MethodAdviceManager;
import org.openjava.probe.agent.advice.MethodPointcut;
import org.openjava.probe.agent.advice.MonitorMethodAdvice;
import org.openjava.probe.agent.asm.ProbeCallback;
import org.openjava.probe.agent.server.ProbeAgentServer;
import org.openjava.probe.agent.session.Session;
import org.openjava.probe.agent.session.SessionState;
import org.openjava.probe.agent.transformer.ClassTransformerManager;
import org.openjava.probe.shared.ErrorCode;
import org.openjava.probe.shared.exception.ProbeServiceException;

public class MonitorCommand extends ProbeCommand<MonitorCommand.MonitorParam> {

    public MonitorCommand(String[] params) throws Exception {
        super(params);
    }

    @Override
    public void execute(Context context) {
        Session session = context.session();
        if (session.compareAndSet(SessionState.IDLE, SessionState.BUSY)) {
            ClassTransformerManager transformerManager = ProbeAgentServer.getInstance().transformerManager();
            ProbeCallback callback = new ProbeCallback() {
                @Override
                public void onProbe(int probeId, Class clazz, String methodName, String methodDesc) {
                    MethodPointcut pointcut = MethodPointcut.of(probeId, clazz, methodName, methodDesc);
                    MonitorMethodAdvice methodAdvice = new MonitorMethodAdvice(pointcut);
                    MethodAdviceManager.getInstance().registerMethodAdvice(probeId, methodAdvice);
                    session.addMethodAdvice(methodAdvice);
                }
            };
            transformerManager.enhance(context.instrumentation(), param.className, param.methodName, callback);
            System.out.println("monitor command execute: " + param.maxTimes);
        } else {
            session.write("Illegal user session state");
        }
    }

    @Override
    public Class<MonitorParam> paramClass() {
        return MonitorParam.class;
    }

    static class MonitorParam extends ProbeCommand.ProbeParam {
        private String className = null;
        private String methodName = null;
        private int maxTimes;

        public MonitorParam(String[] params) {
            super(params);
        }

        @Override
        public void parseParams(String[] params) {
            if (params.length < 2) {
                throw new ProbeServiceException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "Miss params for monitor command");
            }
            this.className = params[0];
            this.methodName = params[1];
            // TODO: parse monitor command params
            for (String param : params) {
                System.out.println(param);
            }
            this.maxTimes = 10;
        }
    }
}
