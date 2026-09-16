package com.framework.exception.login;

import com.framework.constants.ExceptionConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * CSRF错误异常
 *
 * @author dotm
 */
@Slf4j
public class LoginCSRFError extends LoginException {

    public LoginCSRFError(String message) {
        this(message, null);
    }

    public LoginCSRFError(String message, Throwable cause) {
        super(ExceptionConstants.LOGIN_CSRF_ERROR, cause);
        log.info("{}: {}", ExceptionConstants.LOGIN_CSRF_ERROR, message, cause);
    }

}
