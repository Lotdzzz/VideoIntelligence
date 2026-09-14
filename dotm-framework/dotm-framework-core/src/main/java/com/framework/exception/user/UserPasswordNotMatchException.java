package com.framework.exception.user;

import com.framework.constants.ExceptionConstants;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;

/**
 * @author dotm
 * 用户密码不正确或不符合规范异常类
 */
@Slf4j
public class UserPasswordNotMatchException extends UserException {
    @Serial
    private static final long serialVersionUID = 1L;

    public UserPasswordNotMatchException(String message) {
        super(ExceptionConstants.USERNAME_PASSWORD_NOT_MATCH);
        log.info("{}: {}", ExceptionConstants.USERNAME_PASSWORD_NOT_MATCH, message);
    }
}
