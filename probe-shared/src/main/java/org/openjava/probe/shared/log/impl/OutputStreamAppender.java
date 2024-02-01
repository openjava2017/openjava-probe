package org.openjava.probe.shared.log.impl;

import org.openjava.probe.shared.LifeCycle;
import org.openjava.probe.shared.log.Appender;
import org.openjava.probe.shared.log.Encoder;
import org.openjava.probe.shared.log.Layout;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class OutputStreamAppender<T> extends LifeCycle implements Appender<T> {
    private OutputStream outputStream;
    private Encoder<T> encoder;
    private boolean immediateFlush = true;
    protected final Lock lock = new ReentrantLock(false);

    public void append(T event) {
        if (isStarted()) {
            doAppend(event);
        }
    }

    public void setOutputStream(OutputStream outputStream) {
        lock.lock();
        try {
            closeOutputStream();
            this.outputStream = outputStream;
        } finally {
            lock.unlock();
        }
    }

    public void setLayout(Layout<T> layout) {
        this.encoder = new PatternLayoutEncoder<>(layout, null);
    }

    public void setImmediateFlush(boolean immediateFlush) {
        this.immediateFlush = immediateFlush;
    }

    @Override
    protected void doStart() throws Exception {
        if (outputStream == null) {
            throw new IllegalArgumentException("outputSteam cannot be null");
        }

        if (encoder == null) {
            throw new IllegalArgumentException("encoder cannot be null");
        }
    }

    @Override
    protected void doStop() throws Exception {
        lock.lock();
        try {
            closeOutputStream();
        } finally {
            lock.unlock();
        }
    }

    protected void doAppend(T event) {
        byte[] bytes = encoder.encode(event);
        if (bytes != null && bytes.length > 0) {
            lock.lock();
            try {
                outputStream.write(bytes);
                if (immediateFlush) {
                    outputStream.flush();
                }
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            } finally {
                lock.unlock();
            }
        }
    }

    private void closeOutputStream() {
        if (outputStream != null) {
            try {
                outputStream.close();
                outputStream = null;
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }
}
