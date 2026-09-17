package com.dotm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.dotm.constants.OAuthConstants;
import com.dotm.constants.UserConstants;
import com.dotm.entity.dto.oauth.SysAuthOauthDTO;
import com.dotm.entity.dto.system.SysUserDTO;
import com.dotm.entity.model.oauth.SysAuthOauth;
import com.dotm.entity.vo.GithubUserVO;
import com.dotm.entity.vo.SysAuthOauthVO;
import com.dotm.mapper.SysAuthOauthMapper;
import com.dotm.service.SysAuthOauthService;
import com.dotm.service.SysUserService;
import com.framework.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * @author dotm
 * @description 针对表【sys_auth_oauth(第三方账号绑定表)】的数据库操作Service实现
 * @createDate 2026-09-15 10:00:00
 */
@Service
@RequiredArgsConstructor
public class SysAuthOauthServiceImpl extends ServiceImpl<SysAuthOauthMapper, SysAuthOauth>
        implements SysAuthOauthService {

    private final SysAuthOauthMapper sysAuthOauthMapper;

    private final SysUserService sysUserService;

    /**
     * 分页查询第三方账号绑定记录
     */
    @Override
    public IPage<SysAuthOauthVO> pageAuthOauths(SysAuthOauthDTO sysAuthOauthDTO, long pageNum, long pageSize) {
        //创建分页对象
        Page<SysAuthOauth> page = new Page<>(pageNum, pageSize);

        //条件构造器
        LambdaQueryWrapper<SysAuthOauth> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(
                sysAuthOauthDTO.getUserId() != null,
                SysAuthOauth::getUserId,
                sysAuthOauthDTO.getUserId()
        );

        wrapper.eq(
                StringUtils.isNotEmpty(sysAuthOauthDTO.getProvider()),
                SysAuthOauth::getProvider,
                sysAuthOauthDTO.getProvider()
        );

        wrapper.like(
                StringUtils.isNotEmpty(sysAuthOauthDTO.getOpenId()),
                SysAuthOauth::getOpenId,
                sysAuthOauthDTO.getOpenId()
        );

        wrapper.like(
                StringUtils.isNotEmpty(sysAuthOauthDTO.getOauthName()),
                SysAuthOauth::getOauthName,
                sysAuthOauthDTO.getOauthName()
        );

        //按绑定时间倒序
        wrapper.orderByDesc(SysAuthOauth::getCreateTime);

        //执行分页查询
        IPage<SysAuthOauth> authOauthPage = page(page, wrapper);

        //转换为VO分页结果
        Page<SysAuthOauthVO> voPage = new Page<>(pageNum, pageSize, authOauthPage.getTotal());
        voPage.setRecords(authOauthPage.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    /**
     * 根据绑定ID查询第三方账号绑定记录
     */
    @Override
    public SysAuthOauthVO getAuthOauthById(Long oauthId) {
        //根据ID查询绑定实体
        SysAuthOauth sysAuthOauth = getById(oauthId);
        //转换为VO
        return toVO(sysAuthOauth);
    }

    /**
     * 根据系统用户ID查询该用户绑定的第三方账号集合
     */
    @Override
    public List<SysAuthOauthVO> listByUserId(Long userId) {
        //走索引idx_user_id查询该用户已绑定的第三方账号
        List<SysAuthOauth> authOauths = sysAuthOauthMapper.selectByUserId(userId);
        return authOauths.stream().map(this::toVO).toList();
    }

    /**
     * 根据第三方平台和第三方用户唯一ID查询绑定记录
     */
    @Override
    public SysAuthOauthVO getByProviderAndOpenId(String provider, String openId) {
        //走唯一键uk_provider_open_id查询第三方账号归属的系统用户
        SysAuthOauth sysAuthOauth = sysAuthOauthMapper.selectByProviderAndOpenId(provider, openId);
        return toVO(sysAuthOauth);
    }

    /**
     * 绑定第三方账号 已存在同平台同openId的记录时更新为最新信息
     */
    @Override
    @Transactional
    public boolean bindAuthOauth(SysAuthOauth sysAuthOauth) {
        //先按第三方平台和第三方用户唯一ID查询是否已经绑定过
        SysAuthOauth existOauth = sysAuthOauthMapper.selectByProviderAndOpenId(
                sysAuthOauth.getProvider(), sysAuthOauth.getOpenId());

        //已存在绑定记录则更新 避免再次插入触发唯一键uk_provider_open_id冲突
        if (existOauth != null) {
            sysAuthOauth.setOauthId(existOauth.getOauthId());
            return updateById(sysAuthOauth);
        }

        //创建系统用户并绑定
        SysUserDTO sysUserDTO = toSysUserDTO(sysAuthOauth);
        boolean isSave = sysUserService.addUser(sysUserDTO);

        //不存在绑定记录则新增（忽略oauthId，由数据库自增生成）
        sysAuthOauth.setOauthId(null);
        sysAuthOauth.setUserId(sysUserDTO.getUserId());
        boolean save = save(sysAuthOauth);
        return isSave && save;
    }

    /**
     * 解绑第三方账号
     */
    @Override
    public boolean unbindAuthOauth(String provider, Long userId) {
        //根据系统用户ID和第三方平台解绑
        LambdaQueryWrapper<SysAuthOauth> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysAuthOauth::getUserId, userId);
        queryWrapper.eq(SysAuthOauth::getProvider, provider);
        return remove(queryWrapper);
    }

    /**
     * 绑定实体转换为视图对象
     *
     * @param sysAuthOauth 绑定实体
     * @return 绑定视图对象
     */
    private SysAuthOauthVO toVO(SysAuthOauth sysAuthOauth) {
        if (sysAuthOauth == null) {
            return null;
        }
        SysAuthOauthVO vo = new SysAuthOauthVO();
        BeanUtils.copyProperties(sysAuthOauth, vo);
        return vo;
    }

    /**
     * 将Github用户信息转换为SysAuthOauth对象
     *
     * @param githubUser Github用户信息
     * @param userId     系统用户ID
     * @return SysAuthOauth对象
     */
    @Override
    public SysAuthOauth toSysAuthOauth(GithubUserVO githubUser, Long userId) {
        if (githubUser == null || githubUser.id() == null) {
            throw new IllegalArgumentException("GitHub 用户信息或用户 ID 不能为空");
        }

        SysAuthOauth oauth = new SysAuthOauth();
        oauth.setUserId(userId);
        oauth.setProvider(OAuthConstants.GITHUB);

        // GitHub 的 id 是最稳定的唯一标识，用 String 存储避免前端精度丢失
        oauth.setOpenId(String.valueOf(githubUser.id()));

        // GitHub 没有 unionId 概念，这里用 node_id（跨 API 版本稳定）作为统一 ID
        oauth.setUnionId(githubUser.nodeId());

        // 昵称优先取 name，为空则回退到 login（用户名）
        oauth.setOauthName(
                githubUser.name() != null && !githubUser.name().isBlank()
                        ? githubUser.name()
                        : githubUser.login()
        );
        oauth.setOauthAvatar(githubUser.avatarUrl());
        oauth.setOauthEmail(githubUser.email());

        oauth.setRemark("GitHub OAuth 绑定：" + githubUser.login());
        return oauth;
    }

    /**
     * 将SysAuthOauth对象转换为SysUser对象
     *
     * @param oauth SysAuthOauth对象
     * @return SysUser对象
     */
    @Override
    public SysUserDTO toSysUserDTO(SysAuthOauth oauth) {
        if (oauth == null) {
            throw new IllegalArgumentException("第三方绑定信息不能为空");
        }

        SysUserDTO dto = new SysUserDTO();

        // ===== 账号：provider 前缀 + 第三方昵称（清洗非法字符）=====
        dto.setUserName(buildUserName(oauth));

        // ===== 昵称 =====
        dto.setNickName(oauth.getOauthName());

        // ===== 邮箱 =====
        dto.setEmail(oauth.getOauthEmail());

        // ===== 头像 =====
        dto.setAvatar(oauth.getOauthAvatar());

        // ===== 密码：OAuth 用户无密码，存随机串，禁止密码登录 =====
        dto.setPassword(UUID.randomUUID().toString().replace("-", ""));

        // ===== 状态 =====
        dto.setStatus(UserConstants.NORMAL);  // 0 正常

        // ===== 备注 =====
        dto.setRemark("由 " + oauth.getProvider() + " 第三方登录自动创建");

        return dto;
    }

    /**
     * 生成系统账号名：provider 前缀 + 第三方昵称（去掉非法字符）
     * 实际使用中建议外部再加唯一性校验/重试
     */
    private static String buildUserName(SysAuthOauth oauth) {
        String provider = oauth.getProvider() == null ? "oauth" : oauth.getProvider().toLowerCase();
        String base = oauth.getOauthName();
        if (base == null || base.isBlank()) {
            base = oauth.getOpenId();
        }
        // 去掉空格及特殊字符，只保留字母数字下划线
        base = base.replaceAll("[^a-zA-Z0-9_]", "");
        if (base.length() > 20) {
            base = base.substring(0, 20);
        }
        return provider + "_" + base;
    }
}
