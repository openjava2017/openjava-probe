package org.openjava.probe.shared.nio.processor;

import org.openjava.probe.shared.ILifeCycle;
import org.openjava.probe.shared.nio.session.INioSession;

public interface IProcessor<T extends INioSession> extends IProcessorChain, ILifeCycle {
    long id();
    
    void registerWriter(T session);
    
    void unregisterSession(T session);
}
