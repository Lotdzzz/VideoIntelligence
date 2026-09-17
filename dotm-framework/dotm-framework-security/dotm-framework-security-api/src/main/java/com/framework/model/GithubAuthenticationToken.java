package com.framework.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 用于做登录用户认证的pojo类
 *
 * @author dotm
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GithubAuthenticationToken extends AbstractAuthenticationToken {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户传来的系统第三方用户的userId
     *
     * @param userId 用户id
     */
    public GithubAuthenticationToken(Long userId) {
        super((Collection<? extends GrantedAuthority>) null);
        this.userId = userId;
    }

    /**
     * 用户密码验证 因为github的系统第三方用户不需要password所以这里返回null
     *
     * @return null
     */
    @Override
    public @Nullable Object getCredentials() {
        return null;
    }

    /**
     * 用户标识 这里用userId作为用户标识
     *
     * @return userId
     */
    @Override
    public @Nullable Object getPrincipal() {
        return userId;
    }
}
