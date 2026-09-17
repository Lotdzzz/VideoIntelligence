package com.dotm.entity.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 用来存储github回调的结果
 * token 用户信息等
 *
 * @author dotm
 */
@Data
@Builder
public class GithubCallBackVO {

    /**
     * token
     */
    private String token;

    /**
     * github用户信息
     */
    private GithubUserVO githubUserVO;
}
