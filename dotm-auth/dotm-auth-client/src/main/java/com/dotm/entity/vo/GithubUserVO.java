package com.dotm.entity.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 用来接收github返回的具体用户信息的对象
 * jsonProperty注解是用来将json中的字段映射到Java对象的属性上
 *
 * @author dotm
 */
public record GithubUserVO(

        // ===== 基础身份 =====
        Long id,                                        // GitHub 用户唯一 ID（★ 最稳定标识）
        String login,                                   // 用户名
        @JsonProperty("node_id") String nodeId,
        @JsonProperty("type") String type,            // "User" / "Organization"
        @JsonProperty("site_admin") Boolean siteAdmin,

        // ===== 展示信息 =====
        String name,                                    // 昵称，可能为 null
        String email,                                   // 公开邮箱，可能为 null
        String bio,                                     // 个人简介
        String company,
        String location,
        String blog,
        @JsonProperty("twitter_username") String twitterUsername,
        Boolean hireable,                               // 是否可雇佣

        // ===== 头像 / 主页 =====
        @JsonProperty("avatar_url") String avatarUrl,
        @JsonProperty("gravatar_id") String gravatarId,
        @JsonProperty("html_url") String htmlUrl,         // GitHub 主页
        String url,                                     // API 自身的 URL

        // ===== 各类链接 =====
        @JsonProperty("followers_url") String followersUrl,
        @JsonProperty("following_url") String followingUrl,
        @JsonProperty("gists_url") String gistsUrl,
        @JsonProperty("starred_url") String starredUrl,
        @JsonProperty("subscriptions_url") String subscriptionsUrl,
        @JsonProperty("organizations_url") String organizationsUrl,
        @JsonProperty("repos_url") String reposUrl,
        @JsonProperty("events_url") String eventsUrl,
        @JsonProperty("received_events_url") String receivedEventsUrl,

        // ===== 统计 =====
        @JsonProperty("public_repos") Integer publicRepos,
        @JsonProperty("public_gists") Integer publicGists,
        Integer followers,
        Integer following,

        // ===== 时间 =====
        @JsonProperty("created_at") String createdAt,       // 注册时间
        @JsonProperty("updated_at") String updatedAt,

        // ===== 私有 / 企业信息（需 token 权限才能拿到）=====
        @JsonProperty("private_gists") Integer privateGists,
        @JsonProperty("total_private_repos") Integer totalPrivateRepos,
        @JsonProperty("owned_private_repos") Integer ownedPrivateRepos,
        @JsonProperty("disk_usage") Integer diskUsage,
        Integer collaborators,
        @JsonProperty("two_factor_authentication") Boolean twoFactorAuthentication,

        // ===== 嵌套对象 =====
        Plan plan
) {

    /**
     * plan 子对象
     */
    public record Plan(
            String name,                                       // "Medium" / "Free" / "Pro" ...
            Integer space,                                     // 单位 MB
            @JsonProperty("private_repos") Integer privateRepos,
            Integer collaborators
    ) {
    }
}
