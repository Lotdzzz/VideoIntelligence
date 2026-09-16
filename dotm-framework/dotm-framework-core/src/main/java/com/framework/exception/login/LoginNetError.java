package com.framework.exception.login;

import com.framework.constants.ExceptionConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * 网络异常
 *
 * @author dotm
 */
@Slf4j
public class LoginNetError extends LoginException {

    public LoginNetError(String message) {
        this(message, null);
    }

    public LoginNetError(String message, Throwable cause) {
        super(ExceptionConstants.LOGIN_NET_ERROR, cause);
        log.info("{}: {}", ExceptionConstants.LOGIN_NET_ERROR, message, cause);
    }

}
