package com.dotm.entity.dto.oauth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * github授权登录入参对象
 *
 * @author dotm
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GithubOAuthDTO {
    /**
     * 客户端ID
     */
    private String clientId;

    /**
     * 重定向URI
     */
    private String redirectUri;

    /**
     * 权限范围
     */
    private String scope;

    /**
     * 防止CSRF(伪造回调)攻击的随机字符串
     * 攻击者拿到code后，伪造请求发送给服务端，服务端拿到code去换取access_token，攻击者就可以拿到access_token
     * 从而实现用非法github账号绑定本地用户抢走真实用户账号绑定
     */
    private String state;

    /**
     * 授权码
     */
    private String code;
}
