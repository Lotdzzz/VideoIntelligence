package com.framework.handler;

import com.framework.constants.TokenConstants;
import com.framework.model.LoginBodyAuthentication;
import com.framework.security.TokenStore;
import com.framework.utils.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

/**
 * @author dotm
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final TokenStore TokenStore;

    @Resource(name = "handlerExceptionResolver")
    private HandlerExceptionResolver resolver;

    /**
     * 如果没走到最后就放行说明需要登陆或者注册
     * 如果走到最后说明是已经登陆后的请求
     * 1 获取请求携带的jwt令牌
     * 2 验证令牌合法性
     * 3 获取用户指纹
     * 4 校验用户指纹
     * 5 用户信息放入本地上下文
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        //获取uuid
        String uuid = request.getHeader(TokenConstants.UUID);

        if (StringUtils.isNotEmpty(uuid)) {
            //获取用户信息
            LoginBodyAuthentication user = TokenStore.getCacheObject(uuid);

            //放入本地线程上下文
            if (user != null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
