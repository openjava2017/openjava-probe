package org.openjava.probe.shared.exception;

import org.openjava.probe.shared.ErrorCode;

/**
 * 所有模块异常类的基类
 */
public class ProbeServiceException extends RuntimeException {
    /**
     * 错误码
     */
    private int code = ErrorCode.SYSTEM_UNKNOWN_ERROR;

    /**
     * 是否打印异常栈
     */
    private boolean stackTrace = true;

    public ProbeServiceException() {

    }

    public ProbeServiceException(String message) {
        super(message);
    }

    public ProbeServiceException(int code, String message) {
        super(message);
        this.code = code;
        this.stackTrace = false;
    }

    public ProbeServiceException(String message, Throwable ex) {
        super(message, ex);
    }

    @Override
    public Throwable fillInStackTrace() {
        return stackTrace ? super.fillInStackTrace() : this;
    }

    public int getCode() {
        return code;
    }
}
