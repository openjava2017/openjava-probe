package org.openjava.probe.shared.nio.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiException extends Exception {
    private static final long serialVersionUID = -4472194984009202741L;

    private List<Throwable> nested;

    public MultiException() {
        super("Multiple exceptions");
    }

    public void add(Throwable e) {
        if (nested == null) {
            nested = new ArrayList<Throwable>();
        }

        if (e instanceof MultiException) {
            MultiException me = (MultiException) e;
            nested.addAll(me.nested);
        } else {
            nested.add(e);
        }
    }

    public int size() {
        return (nested == null) ? 0 : nested.size();
    }

    public List<Throwable> getThrowables() {
        if (nested == null) {
            return Collections.emptyList();
        }
        return nested;
    }

    public Throwable getThrowable(int i) {
        return nested.get(i);
    }

    /**
     * Throw a multiexception. If this multi exception is empty then no action
     * is taken. If it contains a single exception that is thrown, otherwise the
     * this multi exception is thrown.
     */
    public void ifExceptionThrow() throws Exception {
        if (nested == null) {
            return;
        }

        switch (nested.size()) {
        case 0:
            break;
        case 1:
            Throwable th = nested.get(0);
            if (th instanceof Error) {
                throw (Error) th;
            }

            if (th instanceof Exception) {
                throw (Exception) th;
            }
        default:
            throw this;
        }
    }

    /**
     * Throw a Runtime exception. If this multi exception is empty then no
     * action is taken. If it contains a single error or runtime exception that
     * is thrown, otherwise the this multi exception is thrown, wrapped in a
     * runtime exception.
     */
    public void ifExceptionThrowRuntime() throws Error {
        if (nested == null)
            return;

        switch (nested.size()) {
        case 0:
            break;
        case 1:
            Throwable th = nested.get(0);
            if (th instanceof Error)
                throw (Error) th;
            else if (th instanceof RuntimeException)
                throw (RuntimeException) th;
            else
                throw new RuntimeException(th);
        default:
            throw new RuntimeException(this);
        }
    }

    /**
     * Throw a multiexception. If this multi exception is empty then no action
     * is taken. If it contains a any exceptions then this multi exception is
     * thrown.
     */
    public void ifExceptionThrowMulti() throws MultiException {
        if (nested == null)
            return;

        if (nested.size() > 0)
            throw this;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(MultiException.class.getSimpleName());
        if ((nested == null) || (nested.size() <= 0)) {
            str.append("[]");
        } else {
            str.append(nested);
        }
        return str.toString();
    }

    @Override
    public void printStackTrace() {
        super.printStackTrace();
        if (nested != null) {
            for (Throwable t : nested) {
                t.printStackTrace();
            }
        }
    }

    /**
     * @see Throwable#printStackTrace(PrintStream)
     */
    @Override
    public void printStackTrace(PrintStream out) {
        super.printStackTrace(out);
        if (nested != null) {
            for (Throwable t : nested) {
                t.printStackTrace(out);
            }
        }
    }

    /**
     * @see Throwable#printStackTrace(PrintWriter)
     */
    @Override
    public void printStackTrace(PrintWriter out) {
        super.printStackTrace(out);
        if (nested != null) {
            for (Throwable t : nested) {
                t.printStackTrace(out);
            }
        }
    }
}
