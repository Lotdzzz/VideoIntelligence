package com.framework.exception.user;

import com.framework.constants.ExceptionConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dotm
 * 用户锁定异常
 */
@Slf4j
public class UserLockedException extends UserException {

    public UserLockedException(String message) {
        super(ExceptionConstants.USER_LOCKED);
        log.info("{}: {}", ExceptionConstants.USER_LOCKED, message);
    }
}
