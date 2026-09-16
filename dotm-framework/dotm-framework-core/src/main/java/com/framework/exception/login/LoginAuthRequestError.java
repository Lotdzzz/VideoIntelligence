package com.framework.exception.login;

import com.framework.constants.ExceptionConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证请求失败异常
 *
 * @author dotm
 */
@Slf4j
public class LoginAuthRequestError extends LoginException {

    public LoginAuthRequestError(String message) {
        this(message, null);
    }

    public LoginAuthRequestError(String message, Throwable cause) {
        super(ExceptionConstants.LOGIN_AUTH_REQUEST_ERROR, cause);
        log.info("{}: {}", ExceptionConstants.LOGIN_AUTH_REQUEST_ERROR, message, cause);
    }

}