package com.dotm.aspectj;

import com.dotm.annotation.AuthLog;
import com.framework.constants.AuthLoginLogConstants;
import com.framework.constants.ExceptionConstants;
import com.framework.constants.TokenConstants;
import com.dotm.entity.dto.AuthLogDTO;
import com.dotm.entity.dto.login.LoginBodyDTO;
import com.framework.exception.user.UserLockedException;
import com.framework.exception.user.UserPasswordNotMatchException;
import com.framework.model.LoginBodyAuthentication;
import com.dotm.producer.AuthLogEventProducer;
import com.framework.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author dotm
 * AuthAspectJ 切面类
 * 用于在登录和登出方法执行前进行认证逻辑
 * 获取到目标数据后通过mq发往日志服务进行记录
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthAspectJ {

    private final AuthLogEventProducer authLogEventProducer;

    /**
     * 定义切点
     */
    @SuppressWarnings("all")
    @Pointcut("@annotation(authLog)")
    public void authLoginPointcut(AuthLog authLog) {
        // 切点表达式
    }

    /**
     * 方法执行前
     * 记录目标登录状态
     * 将数据通过生产者mq发送到日志服务进行记录
     * 获取到什么数据就发什么数据 让消费者封装实体类
     */
    @Around(value = "authLoginPointcut(authLog)", argNames = "joinPoint,authLog")
    public Object aroundLoginMethod(ProceedingJoinPoint joinPoint, AuthLog authLog) {
        //进行数据采集
        AuthLogDTO authlogDTO = new AuthLogDTO();
        String username = null;
        String ip = null;
        String userAgent = null;

        //判断是否是退出方法
        if (authLog.type().equals(AuthLoginLogConstants.LOGOUT)) {
            //获取当前用户信息 在这之后不用这样的方法获取因为登录的时候还没有存redis
            LoginBodyAuthentication loginUser = SecurityUtils.getLoginUser();
            username = Objects.nonNull(loginUser) ? loginUser.getUsername() : null;
            //获取ip地址
            HttpServletRequest request = ((ServletRequestAttributes)
                    Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
            ip = request.getHeader(TokenConstants.X_REAL_IP);
            //获取设备指纹
            userAgent = request.getHeader(TokenConstants.USER_AGENT);
            authlogDTO.setStatus(AuthLoginLogConstants.LOGIN_SUCCESS);
            authlogDTO.setLogoutTime(LocalDateTime.now());
        } else {
            //用户名
            username = ((LoginBodyDTO) joinPoint.getArgs()[0]).getUsername();
            //获取ip地址
            HttpServletRequest request = (HttpServletRequest) joinPoint.getArgs()[1];
            ip = request.getHeader(TokenConstants.X_REAL_IP);
            //获取设备指纹
            userAgent = request.getHeader(TokenConstants.USER_AGENT);
            authlogDTO.setStatus(AuthLoginLogConstants.LOGIN_SUCCESS);
        }
        authlogDTO.setUsername(username);
        authlogDTO.setIpAddress(ip);
        authlogDTO.setUserAgent(userAgent);
        authlogDTO.setLoginType(authLog.type());

        //判断只有当用户名正确的时候才处理 其他的异常默认抛出
        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (UserPasswordNotMatchException e) {
            authlogDTO.setStatus(AuthLoginLogConstants.LOGIN_FAIL);
            // 如果是密码错误则记录 其他不记录
            authlogDTO.setFailReason(e.getMessage());
            authLogEventProducer.send(authlogDTO);
            throw new UserPasswordNotMatchException(e.getMessage());
        } catch (LockedException e) {
            authlogDTO.setStatus(AuthLoginLogConstants.LOGIN_FAIL);
            authlogDTO.setFailReason(ExceptionConstants.USER_LOCKED);
            authLogEventProducer.send(authlogDTO);
            throw new UserLockedException(e.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        authLogEventProducer.send(authlogDTO);

        return result;
    }
}
