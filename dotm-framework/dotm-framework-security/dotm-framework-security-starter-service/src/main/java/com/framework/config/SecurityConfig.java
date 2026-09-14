package com.framework.config;

import com.framework.constants.SecurityURLPathConstants;
import com.framework.handler.AuthenticationEntryPointImpl;
import com.framework.handler.CustomAccessDeniedHandler;
import com.framework.handler.JwtAuthenticationTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author dotm
 */
@AutoConfiguration
@EnableMethodSecurity
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnMissingBean(SecurityFilterChain.class)
public class SecurityConfig {
    /**
     * jwt校验过滤器
     */
    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    /**
     * 单点登录
     */
    private final AuthenticationEntryPointImpl authenticationEntryPoint;

    /**
     * 未授权异常
     */
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    /**
     * 配置过滤链
     *
     * @param http security过滤规则
     * @return http
     */
    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) {
        return http
                // CSRF禁用，因为不使用session
                .csrf(AbstractHttpConfigurer::disable)
                // 明确告诉 Spring Security：不要创建 Session，不要存 Cookie
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 不使用表单登录和 HTTP Basic。
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                // 【路径鉴权规则】
                // 【异常处理】
                // 如果没带 Token 访问，返回自定义的 JSON (401)，而不是默认的报错页
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler))
                .authorizeHttpRequests(auth -> {
                    //因为在网关层面以做到白名单所以此处全部放行
                    auth.requestMatchers(HttpMethod.OPTIONS, SecurityURLPathConstants.ALL).permitAll()
                            .requestMatchers(SecurityURLPathConstants.STATIC_RESOURCES).permitAll()
                            .requestMatchers(
                                    SecurityURLPathConstants.LOGIN_URL,
                                    SecurityURLPathConstants.REGISTER_URL,
                                    SecurityURLPathConstants.AI)
                            .permitAll()
                            .anyRequest().authenticated();
                })
                // 【插入过滤器】
                // 把我们的 JWT 过滤器插到 UsernamePasswordAuthenticationFilter 之前
                // 确保请求进来先查 Token
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
