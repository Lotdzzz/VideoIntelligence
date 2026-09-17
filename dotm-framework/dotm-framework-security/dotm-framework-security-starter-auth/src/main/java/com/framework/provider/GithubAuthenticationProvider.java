package com.framework.provider;

import com.framework.exception.user.UserAccountExpired;
import com.framework.exception.user.UserLockedException;
import com.framework.exception.user.UserNotExistsException;
import com.framework.model.GithubAuthenticationToken;
import com.framework.model.LoginBodyAuthentication;
import com.framework.service.IUserDetailsService;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * github的认证provider
 * 告诉security如果有github的认证请求就交给这个provider处理
 *
 * @author dotm
 */
@Component
public class GithubAuthenticationProvider implements AuthenticationProvider {

    private final IUserDetailsService userDetailsService;

    public GithubAuthenticationProvider(IUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * 具体的认证
     * 在这个方法内获取用户信息之后调用userDetailsService的loadUserByUserId等方法获取UserDetails实现类的用户信息
     * 拿到这个用户信息之后就可以返回一个Authentication对象给security了
     * 而且这个类也会被存到redis 当下一次请求的时候security的authenticationTokenFiler会从redis中获取这个类 然后就可以直接获取用户信息了
     *
     * @param authentication the authentication request object.
     * @return a fully authenticated object including credentials.
     * @throws AuthenticationException if authentication fails.
     */
    @Override
    public @Nullable Authentication authenticate(@NonNull Authentication authentication) throws AuthenticationException {
        //获取github的认证token
        GithubAuthenticationToken githubAuthenticationToken = (GithubAuthenticationToken) authentication;

        //获取用户id
        Long userId = githubAuthenticationToken.getUserId();

        //调用loadUserByUserId方法获取用户信息 包括权限列表 角色 菜单等
        LoginBodyAuthentication userDetails = (LoginBodyAuthentication) userDetailsService.loadUserById(userId);

        //校验用户信息合法性 内部抛异常
        checkUserDetails(userDetails);

        //获取用户
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    /**
     * 判断是否支持这个认证类型
     *
     * @param authentication the class of the authentication
     * @return true if the implementation can more specifically evaluate the Authentication class
     */
    @Override
    public boolean supports(@NonNull Class<?> authentication) {
        return GithubAuthenticationToken.class
                .isAssignableFrom(authentication);
    }

    /**
     * 检查用户信息是否合法
     * 这里主要检查
     * 用户过期
     * 用户锁定
     * 密码过期
     * 用户是否启动
     *
     * @param userDetails the user details to check
     */
    private void checkUserDetails(LoginBodyAuthentication userDetails) {
        // 判断用户是否存在
        if (userDetails == null) {
            throw new UserNotExistsException(null);
        }
        // 判断用户是否过期
        if (!userDetails.isAccountNonExpired()) {
            throw new UserAccountExpired(null);
        }
        // 判断用户是否锁定
        if (!userDetails.isAccountNonLocked()) {
            throw new UserLockedException(null);
        }
    }
}
