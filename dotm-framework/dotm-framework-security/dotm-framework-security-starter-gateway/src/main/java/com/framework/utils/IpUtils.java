package com.framework.utils;

import org.springframework.web.server.ServerWebExchange;

import java.util.Objects;

/**
 * @author dotm
 * 获取ip地址工具类
 */
public class IpUtils {

    private IpUtils() {
        throw new UnsupportedOperationException("工具类禁止实例化");
    }

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";

    /**
     * 网关模块获取客户端IP地址
     *
     * @param exchange 请求头
     * @return ip
     */
    public static String getClientIp(ServerWebExchange exchange) {
        if (exchange == null) {
            return UNKNOWN;
        }

        //获取用户公网ip
        String ip = Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress();

        if (LOCALHOST_IPV6.equals(ip)) {
            ip = LOCALHOST_IPV4;
        }
        return ip;
    }
}
