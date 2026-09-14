package com.framework.constants;

/**
 * @author dotm
 * 设置过滤器的优先级，值越小优先级越高
 */
public class FilterOrderConstants {

    /**
     * xss过滤器优先级
     */
    public static final int XSS_FILTER_ORDER = 1;

    /**
     * Security未授权过滤器
     */
    public static final int SECURITY_UNAUTHORIZED_FILTER_ORDER = 2;

    /**
     * 全局异常捕获器
     */
    public static final int GLOBAL_EXCEPTION_FILTER_ORDER = 3;
}
