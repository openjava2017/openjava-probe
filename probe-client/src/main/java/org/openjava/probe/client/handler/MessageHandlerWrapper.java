package org.openjava.probe.client.handler;

import org.openjava.probe.client.context.Context;
import org.openjava.probe.shared.message.Message;
import org.openjava.probe.shared.message.MessageHeader;
import org.openjava.probe.shared.util.AssertUtils;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class MessageHandlerWrapper {
    private static final Map<Integer, Class<? extends MessageHandler>> handlers = new HashMap<>();

    private final Context context;
    private final Message message;

    static {
        handlers.put(MessageHeader.INFO_MESSAGE.getCode(), UserMessageHandler.class);
        handlers.put(MessageHeader.SESSION_STATE.getCode(), SessionStateHandler.class);
        handlers.put(MessageHeader.DUMP_CLASS.getCode(), DumpClassHandler.class);
    }

    public MessageHandlerWrapper(Context context, Message message) {
        this.context = context;
        this.message = message;
    }

    public void handle() throws Exception {
        Class<? extends MessageHandler> clazz = handlers.get(message.header());
        AssertUtils.notNull(clazz, String.format("unrecognized user handler: %s", message.header()));
        Constructor<? extends MessageHandler> constructor = clazz.getConstructor(byte[].class);
        Handler handler = constructor.newInstance(new Object[] {message.payload()});
        handler.handle(context);
    }
}
