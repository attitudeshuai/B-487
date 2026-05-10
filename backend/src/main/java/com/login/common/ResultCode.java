package com.login.common;

/**
 * 响应状态码枚举
 * 
 * @author Login System
 */
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),

    // 服务器错误 5xx
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 业务错误码 1xxx
    USER_NOT_FOUND(1001, "用户不存在"),
    PASSWORD_ERROR(1002, "密码错误"),
    USER_EXISTS(1003, "用户名已存在"),
    EMAIL_EXISTS(1004, "邮箱已被注册"),
    LOGIN_FAILED(1005, "登录失败"),
    REGISTER_FAILED(1006, "注册失败"),
    INVALID_TOKEN(1007, "无效的Token"),
    TOKEN_EXPIRED(1008, "Token已过期"),
    EMAIL_NOT_REGISTERED(1009, "邮箱未注册"),
    CODE_SEND_TOO_FREQUENT(1010, "验证码发送过于频繁，请稍后再试"),
    CODE_SEND_FAILED(1011, "验证码发送失败"),
    CODE_INVALID(1012, "验证码无效或已过期"),
    CODE_ALREADY_USED(1013, "验证码已被使用"),
    PASSWORD_RESET_FAILED(1014, "密码重置失败");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 消息
     */
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
