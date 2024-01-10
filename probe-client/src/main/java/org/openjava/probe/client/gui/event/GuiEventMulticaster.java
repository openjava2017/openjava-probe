package org.openjava.probe.client.gui.event;

import org.openjava.probe.client.console.JavaProcess;

import java.util.function.Consumer;

public class GuiEventMulticaster {

    private static final GuiEventMulticaster eventMulticaster = new GuiEventMulticaster();

    private Consumer<JavaProcess> processConsumer;

    private Consumer<String> commandConsumer;

    private AttachEventListener attachEventListener;

    private DetachEventListener detachEventListener;

    private DataEventListener dataEventListener;

    private SessionStateListener sessionStateListener;

    private DumpEventListener dumpEventListener;

    private GuiEventMulticaster() {
    }

    public static GuiEventMulticaster getInstance() {
        return eventMulticaster;
    }

    public void installProcessConsumer(Consumer<JavaProcess> processConsumer) {
        this.processConsumer = processConsumer;
    }

    public void attachJavaProcess(JavaProcess process) {
        if (processConsumer != null) {
            processConsumer.accept(process);
        }
    }

    public void installCommandConsumer(Consumer<String> commandConsumer) {
        this.commandConsumer = commandConsumer;
    }

    public void sendCommand(String command) {
        if (commandConsumer != null) {
            commandConsumer.accept(command);
        }
    }

    public void installAttachEventListener(AttachEventListener attachEventListener) {
        this.attachEventListener = attachEventListener;
    }

    public void fireAttachEvent(AttachEvent event) {
        if (attachEventListener != null) {
            attachEventListener.onAttach(event);
        }
    }

    public void installDetachEventListener(DetachEventListener detachEventListener) {
        this.detachEventListener = detachEventListener;
    }

    public void fireDetachEvent(DetachEvent event) {
        if (detachEventListener != null) {
            detachEventListener.onDetach(event);
        }
    }

    public void installDataEventListener(DataEventListener dataEventListener) {
        this.dataEventListener = dataEventListener;
    }

    public void fireDataEvent(DataEvent event) {
        if (dataEventListener != null) {
            dataEventListener.dataChange(event);
        }
    }

    public void installSessionStateListener(SessionStateListener sessionStateListener) {
        this.sessionStateListener = sessionStateListener;
    }

    public void fireSessionStateEvent(SessionStateEvent event) {
        if (sessionStateListener != null) {
            sessionStateListener.stateChange(event);
        }
    }

    public void installDumpEventListener(DumpEventListener dumpEventListener) {
        this.dumpEventListener = dumpEventListener;
    }

    public void fireDumpEvent(DumpEvent event) {
        if (dumpEventListener != null) {
            dumpEventListener.onDump(event);
        }
    }
}
