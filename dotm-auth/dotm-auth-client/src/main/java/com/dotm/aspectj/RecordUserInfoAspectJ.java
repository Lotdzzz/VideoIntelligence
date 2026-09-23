package com.dotm.aspectj;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.framework.constants.TokenConstants;
import com.dotm.entity.dto.login.LoginBodyDTO;
import com.dotm.entity.model.system.SysUser;
import com.dotm.service.SysUserService;
import com.framework.exception.user.UserPasswordNotMatchException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author dotm
 * 记录用户信息的切面
 * 记录登录时间、登录IP等信息
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RecordUserInfoAspectJ {

    private final SysUserService sysUserService;

    /**
     * 定义切点获取所有已注解的方法
     */
    @Pointcut("@annotation(com.dotm.annotation.RecordUserInfo)")
    public void recordUserInfoPointcut() {
        // 切点表达式
    }

    /**
     * 环绕通知
     */
    @Around("recordUserInfoPointcut()")
    public Object afterReturning(ProceedingJoinPoint joinPoint) {
        //执行方法
        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (UserPasswordNotMatchException e) {
            throw new UserPasswordNotMatchException(e.getMessage());
        } catch (LockedException e) {
            throw new LockedException(e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        // 获取用户信息
        LoginBodyDTO loginUser = (LoginBodyDTO) joinPoint.getArgs()[0];
        HttpServletRequest request = (HttpServletRequest) joinPoint.getArgs()[1];
        if (loginUser == null || request == null) {
            log.warn("登录用户信息为空，无法记录登录时间和IP");
            return result;
        }

        // 更新用户登录ip 和登录时间
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(SysUser::getLoginIp, request.getHeader(TokenConstants.X_REAL_IP))
                .set(SysUser::getLoginDate, LocalDateTime.now())
                .eq(SysUser::getUserName, loginUser.getUsername());
        sysUserService.update(updateWrapper);
        return result;
    }
}
