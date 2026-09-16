package com.dotm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import com.dotm.entity.dto.oauth.SysAuthOauthDTO;
import com.dotm.entity.dto.system.SysUserDTO;
import com.dotm.entity.model.SysAuthOauth;
import com.dotm.entity.vo.GithubUserVO;
import com.dotm.entity.vo.SysAuthOauthVO;

import java.util.List;

/**
 * @author dotm
 * @description 针对表【sys_auth_oauth(第三方账号绑定表)】的数据库操作Service
 * @createDate 2026-09-15 10:00:00
 */
public interface SysAuthOauthService extends IService<SysAuthOauth> {

    /**
     * 分页查询第三方账号绑定记录
     *
     * @param sysAuthOauthDTO 绑定查询入参
     * @param pageNum         当前页码
     * @param pageSize        每页条数
     * @return 分页结果
     */
    IPage<SysAuthOauthVO> pageAuthOauths(SysAuthOauthDTO sysAuthOauthDTO, long pageNum, long pageSize);

    /**
     * 根据绑定ID查询第三方账号绑定记录
     *
     * @param oauthId 第三方账号绑定ID
     * @return 绑定记录
     */
    SysAuthOauthVO getAuthOauthById(Long oauthId);

    /**
     * 根据系统用户ID查询该用户绑定的第三方账号集合
     *
     * @param userId 系统用户ID
     * @return 绑定记录集合
     */
    List<SysAuthOauthVO> listByUserId(Long userId);

    /**
     * 根据第三方平台和第三方用户唯一ID查询绑定记录
     *
     * @param provider 第三方平台（github、wechat、qq）
     * @param openId   第三方平台用户唯一ID
     * @return 绑定记录
     */
    SysAuthOauthVO getByProviderAndOpenId(String provider, String openId);

    /**
     * 绑定第三方账号 已存在同平台同openId的记录时更新为最新信息
     *
     * @param dto 绑定入参
     * @return 是否成功
     */
    boolean bindAuthOauth(SysAuthOauthDTO dto);

    /**
     * 解绑第三方账号
     *
     * @param provider 第三方平台（github、wechat、qq）
     * @param userId   系统用户ID
     * @return 是否成功
     */
    boolean unbindAuthOauth(String provider, Long userId);

    /**
     * 将Github用户信息转换为SysAuthOauth对象
     *
     * @param githubUser Github用户信息
     * @param userId     系统用户ID
     * @return SysAuthOauth对象
     */
    SysAuthOauth toSysAuthOauth(GithubUserVO githubUser, Long userId);

    /**
     * 将SysAuthOauth对象转换为SysUser对象
     *
     * @param sysAuthOauth SysAuthOauth对象
     * @return SysUser对象
     */
    SysUserDTO toSysUserDTO(SysAuthOauth sysAuthOauth);
}
