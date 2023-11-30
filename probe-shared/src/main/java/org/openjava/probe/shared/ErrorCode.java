package org.openjava.probe.shared;

/**
 * 系统错误码列表 - 错误码前三位用于区分模块
 */
public class ErrorCode {
    // 系统未知异常
    public static final int SYSTEM_UNKNOWN_ERROR = 500000;
    // 无效参数错误
    public static final int ILLEGAL_ARGUMENT_ERROR = 500001;
    // 无效状态错误
    public static final int ILLEGAL_STATE_ERROR = 500002;
    // 操作不允许
    public static final int OPERATION_NOT_ALLOWED = 500003;
    // 访问未授权
    public static final int UNAUTHORIZED_ACCESS_ERROR = 500004;
}
