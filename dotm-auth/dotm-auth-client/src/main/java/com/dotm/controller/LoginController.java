package com.dotm.controller;

import com.dotm.annotation.AuthLog;
import com.dotm.annotation.RecordUserInfo;
import com.framework.constants.AuthLoginLogConstants;
import com.framework.constants.TokenConstants;
import com.dotm.entity.dto.LoginBodyDTO;
import com.dotm.entity.dto.LoginRequestDTO;
import com.dotm.entity.vo.UserInfoVO;
import com.dotm.entity.vo.ValidVO;
import com.framework.model.Result;
import com.dotm.service.SysLoginService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author dotm
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LoginController {

    private final SysLoginService sysLoginService;

    /**
     * 登录接口
     * AuthLog注解是为了让aop模块与认证模块解耦合不让aop模块依赖认证模块
     */
    @PostMapping("/login")
    @AuthLog(type = AuthLoginLogConstants.LOGIN)
    @RecordUserInfo
    public Result<Object> login(@RequestBody LoginBodyDTO loginBodyDTO, HttpServletRequest request) {
        //获取设备信息
        String userAgent = request.getHeader(TokenConstants.USER_AGENT);
        //获取用户真实ip
        String ip = request.getHeader(TokenConstants.X_REAL_IP);
        //获取令牌
        String token = sysLoginService.login(LoginRequestDTO.builder()
                .username(loginBodyDTO.getUsername())
                .password(loginBodyDTO.getPassword())
                .userAgent(userAgent).ip(ip)
                .build());
        Result<Object> result = new Result<>();
        result.put(TokenConstants.TOKEN, token);
        return Result.success(result);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/getUserInfo")
    public Result<UserInfoVO> getUserInfo() {
        //获取用户基本信息
        UserInfoVO userInfoVO = sysLoginService.getUserInfo();
        return Result.success(userInfoVO);
    }

    /**
     * 获取路由信息
     * 根据角色查询对应拥有的菜单
     * 构成菜单树后封装routerVo
     *
     * @return 权限列表 角色列表 路由树
     */
    @GetMapping("/getValid")
    public Result<ValidVO> getValid() {
        //获取 权限列表 角色列表 路由树
        ValidVO result = sysLoginService.getValid();
        return Result.success(result);
    }

    /**
     * 退出登录
     * 同时删除缓存
     */
    @GetMapping("/logout")
    @AuthLog(type = AuthLoginLogConstants.LOGOUT)
    public Result<Object> logout() {
        sysLoginService.logout();
        return Result.success();
    }
}
