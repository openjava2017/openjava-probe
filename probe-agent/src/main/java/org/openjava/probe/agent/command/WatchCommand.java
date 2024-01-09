package org.openjava.probe.agent.command;

import org.openjava.probe.agent.asm.WatchMethodCallback;
import org.openjava.probe.agent.context.Context;
import org.openjava.probe.agent.data.WatchAdviceParam;
import org.openjava.probe.agent.data.WatchMode;
import org.openjava.probe.agent.server.ProbeAgentServer;
import org.openjava.probe.agent.session.Session;
import org.openjava.probe.agent.session.SessionState;
import org.openjava.probe.agent.transformer.ClassTransformerManager;
import org.openjava.probe.shared.message.Message;

public class WatchCommand extends ProbeCommand<WatchCommand.WatchParam> {

    public WatchCommand(String[] params) throws Exception {
        super(params);
    }

    @Override
    public void execute(Context context) {
        Session session = context.session();
        if (session.compareAndSet(SessionState.IDLE, SessionState.BUSY)) {
            ClassTransformerManager transformerManager = ProbeAgentServer.getInstance().transformerManager();
            WatchMethodCallback callback = new WatchMethodCallback(session, WatchAdviceParam.of(param.watchMode, param().maxTimes));
            transformerManager.enhance(context.instrumentation(), param().className, param().methodName, callback);
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
    public Class<WatchParam> paramClass() {
        return WatchParam.class;
    }

    static class WatchParam extends ProbeParam {
        private String className;
        private String methodName;
        private WatchMode watchMode;
        private Integer maxTimes;

        public WatchParam(String[] params) {
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
                    param = param.substring(2, param.length());
                    int index = param.indexOf('=');
                    if (index <= 0 || index >= param.length() - 1) {
                        throw new IllegalArgumentException("Illegal watch command params");
                    }
                    String key = param.substring(0, index);
                    String value = param.substring(index + 1, param.length());
                    if ("maxTime".equalsIgnoreCase(key)) {
                        try {
                            maxTimes = Integer.parseInt(value);
                        } catch (Exception ex) {
                            throw new IllegalArgumentException("Illegal watch maxTime params");
                        }
                    }
                } else if (param.equalsIgnoreCase("-before")) {
                    this.watchMode = WatchMode.BEFORE;
                } else if (param.equalsIgnoreCase("-after")) {
                    this.watchMode = WatchMode.AFTER;
                } else {
                    throw new IllegalArgumentException("Illegal monitor command params");
                }
            }
        }
    }
}
