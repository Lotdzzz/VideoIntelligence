package com.framework.exception.role;

import com.framework.constants.ExceptionConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * @author dotm
 * @description 角色绑定菜单异常
 * @date 2026-08-06 17:04:42
 */
@Slf4j
public class RoleBindMenusException extends RoleException {
    public RoleBindMenusException(String message) {
        super(ExceptionConstants.ROLE_BIND_MENUS);
        log.info("{}: {}", ExceptionConstants.ROLE_BIND_MENUS, message);
    }
}
