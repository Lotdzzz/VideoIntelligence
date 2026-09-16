package com.framework.constants;

/**
 * @author dotm
 */
public class SecurityURLPathConstants {
    public static final String LOGIN_URL = "/auth/login";
    public static final String REGISTER_URL = "/auth/register";
    public static final String ALL = "/**";
    public static final String AI = "/auth/mcp/**";

    public static final String[] DYNAMIC_RESOURCE = {
            "/auth/login",
            "/auth/register",
            "/auth/mcp/**",
            "/auth/oauth/**",
    };

    public static final String[] STATIC_RESOURCES = {
            "/static/**",
            "/css/**",
            "/js/**",
            "/images/**",
            "/webjars/**",
            "/favicon.ico"
    };
}
