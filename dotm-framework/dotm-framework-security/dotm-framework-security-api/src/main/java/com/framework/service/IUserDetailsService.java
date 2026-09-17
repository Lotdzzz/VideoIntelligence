package com.framework.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * UserDetailsService的抽象接口 额外封装一次对实现类做扩展
 *
 * @author dotm
 */
public interface IUserDetailsService extends UserDetailsService {

    /**
     * 用ID来进行用户授权以及校验
     */
    public UserDetails loadUserById(Long id);
}
