package org.openjava.probe.shared.log.impl;

import java.io.IOException;
import java.io.OutputStream;

public class ConsoleAppender<T> extends OutputStreamAppender<T> {
    @Override
    protected void doStart() throws Exception {
        setOutputStream(new ConsoleOutputStream());
        super.doStart();
    }

    private class ConsoleOutputStream extends OutputStream {
        public void write(int b) throws IOException {
            System.out.write(b);
        }

        public void write(byte[] b) throws IOException {
            System.out.write(b);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            System.out.write(b, off, len);
        }

        public void flush() throws IOException {
            System.out.flush();
        }
    }
}
