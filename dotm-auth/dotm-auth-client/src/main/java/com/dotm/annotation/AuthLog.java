package com.dotm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author dotm
 * 用来记录登录和登出日志的注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthLog {
    String type(); // 登录类型 LOGIN登录 LOGOUT退出
}
