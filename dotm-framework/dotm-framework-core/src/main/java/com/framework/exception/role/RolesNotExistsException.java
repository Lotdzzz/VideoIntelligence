package com.framework.exception.role;

import com.framework.constants.ExceptionConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * @description 角色不存在异常
 * @author dotm
 * @date 2026-08-06 17:04:42
 */
@Slf4j
public class RolesNotExistsException extends RoleException {

    public RolesNotExistsException(String message) {
        super(ExceptionConstants.ROLE_NOT_EXISTS);
        log.info("{}: {}", ExceptionConstants.ROLE_NOT_EXISTS, message);
    }
}
