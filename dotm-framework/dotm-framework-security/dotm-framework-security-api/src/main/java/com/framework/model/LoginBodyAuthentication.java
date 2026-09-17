package com.framework.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.framework.constants.StatusLockedConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dotm
 * 校验实体类 也是存入redis的指纹类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LoginBodyAuthentication extends LoginBodyModel implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 设置权限字符串集合 比如admin:user:list等
     */
    private List<String> permissions;

    /**
     * 设置角色字符串集合 比如admin、common等
     */
    private List<String> roleList;

    /**
     * 设置菜单id集合 方便前端拼装路由树
     */
    private List<Long> menuIds;

    /**
     * 用户状态
     */
    private String status;

    public LoginBodyAuthentication() {
    }

    /**
     * 用户权限不需要存入redis直接忽视 不然会报错
     * 往authorities内存储所有权限字符串、角色字符串
     */
    @JsonIgnore
    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> collect = permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        collect.addAll(roleList.stream().map(SimpleGrantedAuthority::new).toList());
        return collect;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return this.password;
    }

    @Override
    @NonNull
    public String getUsername() {
        return this.username;
    }

    /**
     * 以下四个方法必须加上@JsonIgnore不然序列化redis会出错
     * 返回true表示通过检查 false表示不通过检查
     */
    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return !StatusLockedConstants.LOCKED.equals(this.status);
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }
}
