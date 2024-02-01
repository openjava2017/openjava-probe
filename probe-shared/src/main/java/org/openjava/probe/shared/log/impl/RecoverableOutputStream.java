package org.openjava.probe.shared.log.impl;

import java.io.IOException;
import java.io.OutputStream;

public abstract class RecoverableOutputStream extends OutputStream {
    private static final long BACKOFF_COEFFICIENT_MAX = 327680L;

    private OutputStream outputStream;
    private RecoverStrategy strategy;
    private boolean presumedClean = true;

    public RecoverableOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void write(byte[] b, int off, int len) {
        if (isPresumedInError()) {
            if (!strategy.isTooSoon()) {
                attemptRecovery();
            }
        } else {
            try {
                outputStream.write(b, off, len);
                postSuccessfulWrite();
            } catch (IOException ex) {
                postIOFailure(ex);
            }
        }
    }

    public void write(int b) {
        if (isPresumedInError()) {
            if (!strategy.isTooSoon()) {
                attemptRecovery();
            }
        } else {
            try {
                outputStream.write(b);
                postSuccessfulWrite();
            } catch (IOException ex) {
                postIOFailure(ex);
            }
        }
    }

    public void flush() {
        if (outputStream != null) {
            try {
                outputStream.flush();
                postSuccessfulWrite();
            } catch (IOException ex) {
                postIOFailure(ex);
            }
        }
    }

    public void close() throws IOException {
        if (outputStream != null) {
            outputStream.close();
        }
    }

    protected void attemptRecovery() {
        try {
            close();
        } catch (IOException var3) {
        }

        try {
            outputStream = this.openNewOutputStream();
            presumedClean = true;
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    protected abstract OutputStream openNewOutputStream() throws IOException;

    private boolean isPresumedInError() {
        return strategy != null && !presumedClean;
    }

    private void postSuccessfulWrite() {
        if (strategy != null) {
            strategy = null;
        }
    }

    private void postIOFailure(IOException ex) {
        presumedClean = false;
        if (strategy == null) {
            strategy = new RecoverStrategy();
        }
    }

    private class RecoverStrategy {
        private long backOffCoefficient = 20L;
        private long currentTime;
        private long next;

        public RecoverStrategy() {
            currentTime = -1;
            next = getCurrentTime() + getBackoffCoefficient();
        }

        public boolean isTooSoon() {
            long now = getCurrentTime();
            if (now > next) {
                next = now + getBackoffCoefficient();
                return false;
            } else {
                return true;
            }
        }

        private long getBackoffCoefficient() {
            long current = backOffCoefficient;
            if (backOffCoefficient < BACKOFF_COEFFICIENT_MAX) {
                backOffCoefficient *= 4L;
            }

            return current;
        }

        private long getCurrentTime() {
            return currentTime != -1 ? currentTime : System.currentTimeMillis();
        }
    }
}
