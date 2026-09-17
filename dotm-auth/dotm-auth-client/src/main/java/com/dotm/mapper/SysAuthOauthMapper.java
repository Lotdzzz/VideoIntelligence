package com.dotm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dotm.entity.model.oauth.SysAuthOauth;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author dotm
* @description 针对表【sys_auth_oauth(第三方账号绑定表)】的数据库操作Mapper
* @createDate 2026-09-15 10:00:00
* @Entity com.dotm.entity.model.oauth.SysAuthOauth
*/
public interface SysAuthOauthMapper extends BaseMapper<SysAuthOauth> {

    /**
     * 根据第三方平台和第三方用户唯一ID查询绑定记录 走唯一键 uk_provider_open_id
     *
     * @param provider 第三方平台（github、wechat、qq）
     * @param openId   第三方平台用户唯一ID
     * @return 绑定记录
     */
    SysAuthOauth selectByProviderAndOpenId(@Param("provider") String provider, @Param("openId") String openId);

    /**
     * 根据系统用户ID查询绑定记录集合 走索引 idx_user_id
     *
     * @param userId 系统用户ID
     * @return 绑定记录集合
     */
    List<SysAuthOauth> selectByUserId(@Param("userId") Long userId);
}
