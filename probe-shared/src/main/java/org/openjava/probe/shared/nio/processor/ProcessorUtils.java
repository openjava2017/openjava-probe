package org.openjava.probe.shared.nio.processor;

import org.openjava.probe.shared.nio.session.INioSession;
import org.openjava.probe.shared.nio.session.SessionContext;

import java.io.Closeable;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class ProcessorUtils {
    public static class StopCommand implements Runnable {
        private Selector selector;
        
        private StopCommand(Selector selector) {
            this.selector = selector;
        }
        
        public static StopCommand create(Selector selector) {
            return new StopCommand(selector);
        }
        
        @Override
        public void run() {
            for (SelectionKey key : selector.keys()) {
                Object attachment = key.attachment();
                if (key.isValid()) {
                    key.cancel();
                }
                closeQuietly(key.channel());

                if (attachment instanceof SessionContext) {
                    SessionContext context = (SessionContext) attachment;
                    context.fireSessionClosed();
                }
            }
            closeQuietly(selector);
        }
    }
    
    public static class CloseCommand implements Runnable {
        private INioSession session;
        
        private CloseCommand(INioSession session) {
            this.session = session;
        }
        
        public static CloseCommand create(INioSession session) {
            return new CloseCommand(session);
        }
        
        @Override
        public void run() {
            if (session != null) {
                SelectionKey key = session.getSelectionKey();
                if (key != null && key.isValid()) {
                    key.cancel();
                }
                
                closeQuietly(session.getChannel());
            }
        }
    }
    
    public static void closeQuietly(Closeable closable) {
        if (closable != null) {
            try {
                closable.close();
            } catch (Exception ex) {
            }
        }
    }
}
