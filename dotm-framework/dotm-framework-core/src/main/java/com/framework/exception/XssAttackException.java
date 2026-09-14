package com.framework.exception;

import lombok.Getter;

import java.io.Serial;

/**
 * @author dotm
 */
public final class XssAttackException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    @Getter
    private Integer code;

    /**
     * 错误提示
     */
    private String message;

    /**
     * 空构造方法，避免反序列化问题
     */
    public XssAttackException() {
    }

    public XssAttackException(String message) {
        this.message = message;
    }

    public XssAttackException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
