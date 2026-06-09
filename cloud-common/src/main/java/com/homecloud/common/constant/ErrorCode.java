package com.homecloud.common.constant;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USERNAME_EXISTS(10001, "用户名已存在"),
    PASSWORD_ERROR(10002, "密码错误"),
    USER_DISABLED(10003, "账号已被禁用"),
    TOKEN_EXPIRED(10004, "登录已过期，请重新登录"),
    FILE_NOT_FOUND(20001, "文件不存在"),
    STORAGE_FULL(20002, "存储空间不足"),
    UPLOAD_FAILED(20003, "文件上传失败"),
    FILE_TYPE_NOT_ALLOWED(20004, "不支持的文件类型"),
    RATE_LIMITED(40001, "操作过于频繁，请稍后再试"),
    INTERNAL_ERROR(50000, "服务器内部错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
