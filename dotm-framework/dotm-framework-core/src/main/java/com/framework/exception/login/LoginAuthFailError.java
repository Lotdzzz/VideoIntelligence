package com.framework.exception.login;

import com.framework.constants.ExceptionConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * 登录认证失败异常
 *
 * @author dotm
 */
@Slf4j
public class LoginAuthFailError extends LoginException {

    public LoginAuthFailError(String message) {
        this(message, null);
    }

    public LoginAuthFailError(String message, Throwable cause) {
        super(ExceptionConstants.LOGIN_AUTH_FAIL, cause);
        log.info("{}: {}", ExceptionConstants.LOGIN_AUTH_FAIL, message, cause);
    }

}