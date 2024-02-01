package org.openjava.probe.shared.log.impl;

import java.io.*;

public class FileAppender<T> extends OutputStreamAppender<T> {
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private final File file;
    private final boolean append;

    public FileAppender(String fileName, boolean append) {
        this(new File(fileName), append);
    }

    public FileAppender(File file, boolean append) {
        this.file = file;
        this.append = append;
    }

    @Override
    protected void doStart() throws Exception {
        if (this.file == null) {
            throw new IllegalArgumentException("file cannot be null");
        }

        this.lock.lock();

        try {
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
                if (!parent.exists()) {
                    throw new IllegalAccessException("Failed to create parent directories for [" + file.getAbsolutePath() + "]");
                }
            }

            FileOutputStream fos = new FileOutputStream(file, append);
            OutputStream os = new RecoverableOutputStream(new BufferedOutputStream(fos, DEFAULT_BUFFER_SIZE)) {
                @Override
                protected OutputStream openNewOutputStream() throws IOException {
                    return new BufferedOutputStream(new FileOutputStream(file, true), DEFAULT_BUFFER_SIZE);
                }
            };
            setOutputStream(os);
        } finally {
            this.lock.unlock();
        }
    }
}
