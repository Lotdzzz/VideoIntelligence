package com.dotm.controller;

import com.dotm.entity.dto.oauth.GithubOAuthCodeDTO;
import com.dotm.entity.vo.GithubCallBackVO;
import com.dotm.service.GithubOAuthService;
import com.framework.constants.TokenConstants;
import com.framework.model.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 这是一个oauth适配器 用于github weChat qq等第三方登录
 * github登录流程：
 * 1. 用户点击登录按钮，后端接口收到请求
 * 2. 后端接口向github发送请求，获取url
 *
 * @author dotm
 */
@RestController
@RequestMapping("/auth/oauth")
@RequiredArgsConstructor
public class Oauth2Controller {

    private final GithubOAuthService githubOAuthService;

    /**
     * github接口
     * 构造url后请求github 获取登录url返回前端
     */
    @PostMapping("/github/login")
    public Result<Object> githubLogin() {
        String url = githubOAuthService.getAuthorizationURL();
        return Result.success(url);
    }

    /**
     * github或前端回调接口 完全兼容前端或github回调
     *
     * @return 返回真实的github用户信息
     */
    @GetMapping("/github/callback")
    public Result<GithubCallBackVO> githubCallback(GithubOAuthCodeDTO githubOAuthCodeDTO, HttpServletRequest request) {
        //获取设备信息
        String userAgent = request.getHeader(TokenConstants.USER_AGENT);
        //获取用户真实ip
        String ip = request.getHeader(TokenConstants.X_REAL_IP);

        githubOAuthCodeDTO.setUserAgent(userAgent);
        githubOAuthCodeDTO.setIp(ip);
        // 通过github获取用户信息
        GithubCallBackVO githubCallBackVO = githubOAuthService.githubCallBackHandler(githubOAuthCodeDTO);
        return Result.success(githubCallBackVO);
    }
}
