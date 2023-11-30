package org.openjava.probe.shared.nio.exception;

import org.openjava.probe.shared.ErrorCode;

import java.io.IOException;

/**
 * 所有模块异常类的基类
 */
public class NioSessionException extends IOException {
    /**
     * 错误码
     */
    private int code = ErrorCode.SYSTEM_UNKNOWN_ERROR;

    /**
     * 是否打印异常栈
     */
    private boolean stackTrace = true;

    public NioSessionException() {

    }

    public NioSessionException(String message) {
        super(message);
    }

    public NioSessionException(int code, String message) {
        super(message);
        this.code = code;
        this.stackTrace = false;
    }

    public NioSessionException(String message, Throwable ex) {
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
