package com.framework.exception;

import com.framework.constants.FilterOrderConstants;
import com.framework.exception.login.LoginException;
import com.framework.exception.upload.UploadException;
import com.framework.exception.user.UserException;
import com.framework.model.Result;
import com.framework.utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author dotm
 */
@RestControllerAdvice
@Slf4j
@Order(FilterOrderConstants.GLOBAL_EXCEPTION_FILTER_ORDER)
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(ServiceException.class)
    public Result<Object> handleServiceException(ServiceException e, HttpServletRequest request) {
        log.error(e.getMessage(), e);
        Integer code = e.getCode();
        return StringUtils.isNotNull(code) ? Result.error(code, e.getMessage()) : Result.error(e.getMessage());
    }

    /**
     * 用户异常
     */
    @ExceptionHandler(UserException.class)
    public Result<Object> userException(UserException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生用户异常.", requestURI, e);
        return Result.error(e.getMessage());
    }

    /**
     * 登录异常
     */
    @ExceptionHandler(LoginException.class)
    public Result<Object> loginException(LoginException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生登录异常.", requestURI, e);
        return Result.error(e.getMessage());
    }

    /**
     * xss攻击
     */
    @ExceptionHandler(XssAttackException.class)
    public Result<Object> xssAttackException(XssAttackException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生XSS攻击.", requestURI, e);
        return Result.error(e.getMessage());
    }

    /**
     * 文件上传异常
     */
    @ExceptionHandler(UploadException.class)
    public Result<Object> fileUploadException(UploadException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生文件上传异常.", requestURI, e);
        return Result.error(e.getMessage());
    }
}