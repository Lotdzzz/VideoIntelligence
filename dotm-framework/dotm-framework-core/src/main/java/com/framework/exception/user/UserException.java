package com.framework.exception.user;


import com.framework.constants.ExceptionConstants;

import java.io.Serial;

/**
 * @author dotm
 * 用户信息异常类
 */
public class UserException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public UserException(String code) {
        super(ExceptionConstants.USER + code, null);
    }
}
