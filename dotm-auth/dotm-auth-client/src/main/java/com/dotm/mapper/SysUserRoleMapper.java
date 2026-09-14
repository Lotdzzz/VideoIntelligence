package com.dotm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dotm.entity.model.SysUserRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author dotm
* @description 针对表【sys_user_role(用户和角色关联表)】的数据库操作Mapper
* @createDate 2026-08-06 17:04:42
* @Entity com.videogpt.entity.model.SysUserRole
*/
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * 根据用户id绑定角色列表
     *
     * @param userId  用户id
     * @param roleIds 角色id列表
     */
    boolean bindRolesByUserId(@Param("userId") Long userId,@Param("roleIds") List<Long> roleIds);
}

