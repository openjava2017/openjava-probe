package org.openjava.probe.agent.handler;

import org.openjava.probe.agent.context.Context;
import org.openjava.probe.shared.exception.ProbeServiceException;
import org.openjava.probe.shared.message.Message;
import org.openjava.probe.shared.message.MessageHeader;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class UserMessageHandler  {
    private static final Map<Integer, Class<? extends MessageHandler>> handlers = new HashMap<>();

    static {
        handlers.put(MessageHeader.USER_COMMAND.getCode(), UserCommandHandler.class);
    }

    private Context context;
    private Message message;

    public UserMessageHandler(Context context, Message message) {
        this.context = context;
        this.message = message;
    }

    public void handle() {
        Class<? extends MessageHandler> clazz = handlers.get(message.header());
        if (clazz == null) {
            context.session().write(Message.error(String.format("unrecognized user message: %s", message.header())));
            return;
        }

        try {
            Constructor<? extends MessageHandler> constructor = clazz.getConstructor(byte[].class);
            MessageHandler handler = constructor.newInstance(new Object[]{message.payload()});
            handler.handle(context);
        } catch (ProbeServiceException sex) {
            context.session().write(Message.error(sex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            context.session().write(Message.error("user message handle unknown error"));
        }
    }
}
