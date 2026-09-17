package com.framework.exception.user;

import com.framework.constants.ExceptionConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户账号过期异常
 */
@Slf4j
public class UserAccountExpired extends UserException {

    public UserAccountExpired(String message) {
        super(ExceptionConstants.USER_ACCOUNT_EXPIRED);
        log.info("{}, {}", ExceptionConstants.USER_ACCOUNT_EXPIRED, message);
    }

}
