package com.framework.exception.login;

import com.framework.constants.ExceptionConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户授权失败异常
 *
 * @author dotm
 */
@Slf4j
public class LoginAuthError extends LoginException {

    public LoginAuthError(String message) {
        this(message, null);
    }

    public LoginAuthError(String message, Throwable cause) {
        super(ExceptionConstants.LOGIN_AUTH_ERROR, cause);
        log.info("{}: {}", ExceptionConstants.LOGIN_AUTH_ERROR, message, cause);
    }

}
