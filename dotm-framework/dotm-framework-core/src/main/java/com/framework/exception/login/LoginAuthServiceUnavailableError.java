package com.framework.exception.login;

import com.framework.constants.ExceptionConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * 登录认证服务暂不可用异常
 *
 * @author dotm
 */
@Slf4j
public class LoginAuthServiceUnavailableError extends LoginException {

    public LoginAuthServiceUnavailableError(String message) {
        this(message, null);
    }

    public LoginAuthServiceUnavailableError(String message, Throwable cause) {
        super(ExceptionConstants.LOGIN_AUTH_SERVICE_UNAVAILABLE, cause);
        log.info("{}: {}", ExceptionConstants.LOGIN_AUTH_SERVICE_UNAVAILABLE, message, cause);
    }

}