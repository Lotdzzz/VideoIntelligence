package com.framework.config;

import com.framework.provider.GithubAuthenticationProvider;
import com.framework.service.IUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author dotm
 */
@Configuration
public class AuthenticationConfig {

    /**
     * 数据库用户认证提供者。
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            BCryptPasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * github用户认证提供者
     */
    @Bean
    public GithubAuthenticationProvider githubAuthenticationProvider(
            IUserDetailsService userDetailsService) {
        return new GithubAuthenticationProvider(userDetailsService);
    }

    /**
     * 手动配置AuthenticationManager
     *
     * @param authenticationProvider 适配器
     * @return AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(
            DaoAuthenticationProvider authenticationProvider,
            GithubAuthenticationProvider githubAuthenticationProvider) {
        return new ProviderManager(new ArrayList<>(Arrays.asList(
                authenticationProvider,
                githubAuthenticationProvider
        )));
    }

    /**
     * 登录接口调用 AuthenticationManager 进行用户名密码认证。
     * 自动扫描所有的 AuthenticationProvider 进行认证。
     *
     * @deprecated 该方法已过时，建议使用自定义的 AuthenticationManager Bean。
     */
    @Deprecated
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * 强散列哈希加密实现
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
