package org.openjava.probe.agent.command;

import org.openjava.probe.agent.context.Context;

public class ExitCommand extends ProbeCommand<ProbeCommand.NoneParam> {
    public ExitCommand(String[] params) throws Exception {
        super(params);
    }

    @Override
    public void execute(Context context) {
        context.session().destroy();
    }

    @Override
    public Class<NoneParam> paramClass() {
        return NoneParam.class;
    }
}
