package com.dotm.filter;

import com.framework.constants.ExceptionConstants;
import com.framework.constants.TokenConstants;
import com.framework.model.LoginBodyModel;
import com.framework.properties.IgnoreWhiteProperties;
import com.framework.service.GatewayTokenService;
import com.framework.utils.IpUtils;
import com.dotm.utils.ServletUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @author dotm
 */
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter {

    private final GatewayTokenService tokenService;

    private final IgnoreWhiteProperties ignoreWhiteProperties;

    /**
     * 网关过滤器替代security模块的白名单
     * 检验token
     * 获取redis用户指纹
     * 设备指纹校验
     *
     * @param exchange the current server exchange
     * @param chain    provides a way to delegate to the next filter
     */
    @Override
    @NonNull
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        //获取用户公网ip
        String ip = IpUtils.getClientIp(exchange);

        //白名单判断 如果是白名单则把用户真实ip一起送过去 这里的ip和下面的判断是隔离的
        String path = exchange.getRequest().getURI().getPath();
        if (isIgnore(path)) {
            ServerHttpRequest newRequest = request.mutate()
                    //删除客户端伪造数据
                    .headers(h -> h.remove(TokenConstants.UUID))
                    //添加Ip
                    .headers(h -> h.add(TokenConstants.X_REAL_IP, ip))
                    .build();

            return chain.filter(exchange.mutate()
                    .request(newRequest)
                    .build());
        }

        //获取Authorization头
        String authorization = request.getHeaders().getFirst(TokenConstants.HEADER_STRING);

        //获取设备指纹
        String userAgent = request.getHeaders().getFirst(TokenConstants.USER_AGENT);

        //如果令牌存在且以Bearer开头则开始令牌验证与取信息
        if (authorization == null || !authorization.startsWith(TokenConstants.TOKEN_PREFIX + " ")) {
            return ServletUtils.webFluxResponseWriter(exchange.getResponse(), ExceptionConstants.TOKEN_INVALID);
        }

        //剔除jwt前缀
        String jwt = tokenService.extractTokenFromHeader(authorization, TokenConstants.TOKEN_PREFIX);

        //开始对jwt进行防伪验证
        if (!tokenService.validateToken(jwt)) {
            return ServletUtils.webFluxResponseWriter(exchange.getResponse(), ExceptionConstants.TOKEN_INVALID);
        }

        //指纹校验与令牌刷新
        LoginBodyModel userDetails = tokenService.detailsValidateRefreshToken(jwt);

        if (userDetails == null) {
            return ServletUtils.webFluxResponseWriter(exchange.getResponse(), ExceptionConstants.TOKEN_INVALID);
        }

        //设备指纹校验
        if (!Objects.equals(userAgent, userDetails.getUserAgent())) {
            return ServletUtils.webFluxResponseWriter(exchange.getResponse(), ExceptionConstants.LOGIN_ADDR_ERROR);
        }

        //判断ip
        if (!Objects.equals(ip, userDetails.getIp())) {
            return ServletUtils.webFluxResponseWriter(exchange.getResponse(), ExceptionConstants.LOGIN_IP_ERROR);
        }

        //重新构建请求 只传uuid 用户信息获取交给下游服务
        ServerHttpRequest newRequest = request.mutate()
                //删除客户端伪造数据
                .headers(h -> h.remove(TokenConstants.UUID))
                //添加uuid
                .headers(h -> h.add(TokenConstants.UUID, userDetails.getUuid()))
                .build();

        return chain.filter(exchange.mutate()
                .request(newRequest)
                .build());
    }

    /**
     * 白名单校验
     *
     * @param path 路径
     * @return 真假
     */
    private boolean isIgnore(String path) {
        return ignoreWhiteProperties
                .getUrls()
                .stream()
                .anyMatch(path::startsWith);
    }
}
