package com.framework.exception.role;

import com.framework.constants.ExceptionConstants;

import java.io.Serial;

/**
 * @author dotm
 * 角色异常
 */
public class RoleException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public RoleException(String code) {
        super(ExceptionConstants.ROLE + code, null);
    }
}
