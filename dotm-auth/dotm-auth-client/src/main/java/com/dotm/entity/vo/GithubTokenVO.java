package com.dotm.entity.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 用来接受github返回的access_token的对象
 *
 * @param accessToken      The access token
 * @param tokenType        The token type
 * @param scope            The scope
 * @param error            The error
 * @param errorDescription The error description
 * @param errorUri         The error URI
 * @author dotm
 */
public record GithubTokenVO(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("scope") String scope,
        @JsonProperty("error") String error,
        @JsonProperty("error_description") String errorDescription,
        @JsonProperty("error_uri") String errorUri
) {
    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return accessToken != null && !accessToken.isBlank() && error == null;
    }
}
