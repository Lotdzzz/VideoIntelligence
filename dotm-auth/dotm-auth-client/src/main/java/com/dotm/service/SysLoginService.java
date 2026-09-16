package com.dotm.service;

import com.dotm.entity.dto.login.LoginRequestDTO;
import com.dotm.entity.vo.UserInfoVO;
import com.dotm.entity.vo.ValidVO;

/**
 * @author dotm
 */
public interface SysLoginService {

    /**
     * 登录接口
     */
    String login(LoginRequestDTO loginRequestDTO);

    /**
     * 登录前置校验
     * @param username 用户名
     * @param password 用户密码
     */
    void loginPreCheck(String username, String password);

    /**
     * 获取用户信息
     * @return 用户信息
     */
    UserInfoVO getUserInfo();

    /**
     * 获取路由信息
     * 根据角色查询对应拥有的菜单
     * 构成菜单树后封装routerVo
     *
     * @return 权限列表 角色列表 路由树
     */
    ValidVO getValid();

    /**
     * 登出接口
     * 同时删除缓存
     */
    void logout();
}
