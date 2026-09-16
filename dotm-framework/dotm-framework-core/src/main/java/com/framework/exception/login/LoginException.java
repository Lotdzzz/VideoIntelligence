package com.framework.exception.login;

import java.io.Serial;

/**
 * 登录异常
 *
 * @author dotm
 */
public class LoginException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public LoginException(String message) {
        this(message, null);
    }

    public LoginException(String message, Throwable cause) {
        super(message, cause);
    }

}
