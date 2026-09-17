package com.dotm.service.impl;

import com.dotm.constants.UserConstants;
import com.dotm.entity.dto.login.LoginRequestDTO;
import com.dotm.entity.model.system.SysMenu;
import com.dotm.entity.model.system.SysUser;
import com.dotm.entity.vo.MetaVO;
import com.dotm.entity.vo.RouterVO;
import com.dotm.entity.vo.UserInfoVO;
import com.dotm.entity.vo.ValidVO;
import com.framework.exception.user.UserNotExistsException;
import com.framework.exception.user.UserPasswordNotMatchException;
import com.framework.model.LoginBodyAuthentication;
import com.framework.service.AuthTokenService;
import com.dotm.service.SysLoginService;
import com.dotm.service.SysMenuService;
import com.dotm.service.SysUserService;
import com.framework.utils.SecurityUtils;
import com.framework.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dotm
 */
@Service
@RequiredArgsConstructor
public class SysLoginServiceImpl implements SysLoginService {

    private final AuthenticationManager authenticationManager;

    private final AuthTokenService tokenService;

    private final SysUserService sysUserService;

    private final SysMenuService sysMenuService;

    /**
     * 登录接口
     * 校验用户名密码合法性
     * 校验用户名密码正确性
     * 生成token
     */
    @Override
    public String login(LoginRequestDTO loginRequestDTO) {
        String username = loginRequestDTO.getUsername();
        String password = loginRequestDTO.getPassword();
        String userAgent = loginRequestDTO.getUserAgent();
        //登录前置校验
        loginPreCheck(username, password);

        // 用户验证
        Authentication authentication = null;
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(authenticationToken);
        } catch (UserPasswordNotMatchException | BadCredentialsException e) {
            throw new UserPasswordNotMatchException(e.getMessage());
        } catch (UserNotExistsException e) {
            throw new UserNotExistsException(e.getMessage());
        }

        //获取登录后的用户
        LoginBodyAuthentication sysUser = (LoginBodyAuthentication) authentication.getPrincipal();

        if (sysUser == null) {
            throw new UserNotExistsException(null);
        }

        sysUser.setUserAgent(userAgent);
        sysUser.setIp(loginRequestDTO.getIp());

        //生成token 需要用户名 设备指纹
        return tokenService.createJwtToken(sysUser);
    }

    /**
     * 登录前置校验
     *
     * @param username 用户名
     * @param password 用户密码
     */
    @Override
    public void loginPreCheck(String username, String password) {
        // 用户名或密码为空 错误
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new UserNotExistsException(null);
        }
        // 密码如果不在指定范围内 错误
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            throw new UserPasswordNotMatchException(null);
        }
        // 用户名不在指定范围内 错误
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            throw new UserPasswordNotMatchException(null);
        }
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @Override
    public UserInfoVO getUserInfo() {
        //获取userId
        Long userId = SecurityUtils.getUserId();

        //查询用户实体
        SysUser sysUser = sysUserService.getById(userId);

        //copy
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(sysUser, userInfoVO);

        return userInfoVO;
    }

    /**
     * 获取路由信息
     * 根据角色查询对应拥有的菜单
     * 构成菜单树后封装routerVo
     *
     * @return 权限列表 角色列表 路由树
     */
    @Override
    public ValidVO getValid() {
        ValidVO validVO = new ValidVO();

        //获取用户拥有的权限
        List<Long> menuIds = SecurityUtils.getLoginUser().getMenuIds();

        if (menuIds == null || menuIds.isEmpty()) {
            return validVO;
        }

        List<SysMenu> sysMenus = sysMenuService.listByIds(menuIds);

        //过滤掉按钮类型的菜单，因为按钮不需要在前端路由中显示
        sysMenus = sysMenus.stream()
                .filter(menu -> !UserConstants.TYPE_BUTTON.equals(menu.getMenuType()))
                .toList();
        List<SysMenu> trees = sysMenuService.buildTree(sysMenus);

        //拼装
        validVO.setPermissions(SecurityUtils.getLoginUser().getPermissions());
        validVO.setRoles(SecurityUtils.getLoginUser().getRoleList());

        //拼装路由树
        List<RouterVO> routes = buildRouterTree(trees);
        validVO.setRouters(routes);

        return validVO;
    }

    /**
     * 构建路由树
     *
     * @param menus 菜单列表
     * @return 路由树
     */
    private List<RouterVO> buildRouterTree(List<SysMenu> menus) {
        List<RouterVO> routers = new ArrayList<>();

        for (SysMenu menu : menus) {

            //按钮不生成路由
            if (UserConstants.TYPE_BUTTON.equals(menu.getMenuType())) {
                continue;
            }
            RouterVO router = new RouterVO();
            //name
            router.setName(getRouteName(menu));
            //path
            router.setPath(getRouterPath(menu));
            //component
            router.setComponent(getComponent(menu));
            //hidden
            router.setHidden(UserConstants.VISIBLE_HIDE.equals(menu.getVisible()));
            //query
            router.setQuery(menu.getQuery());
            //meta
            MetaVO meta = new MetaVO();
            meta.setTitle(menu.getMenuName());
            meta.setIcon(menu.getIcon());
            meta.setPerms(menu.getPerms());
            meta.setKeepAlive(menu.getIsCache() == 0);
            meta.setFrame(menu.getIsFrame() == 0);
            router.setMeta(meta);
            //children
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                router.setAlwaysShow(true);
                router.setRedirect(UserConstants.NO_REDIRECT);
                router.setChildren(buildRouterTree(menu.getChildren()));
            }
            routers.add(router);
        }
        return routers;
    }

    /**
     * 获取路由名称
     *
     * @param menu 菜单实体
     * @return 路由名称
     */
    private String getRouteName(SysMenu menu) {
        if (menu.getRouteName() != null && !menu.getRouteName().isEmpty()) {
            return menu.getRouteName();
        }
        return capitalize(menu.getPath());
    }

    /**
     * 获取路由路径
     *
     * @param str 路由路径
     * @return 首字母大写的路由路径
     */
    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 获取路由路径
     *
     * @param menu 菜单实体
     * @return 路由路径
     */
    private String getComponent(SysMenu menu) {
        //目录
        if (UserConstants.TYPE_DIR.equals(menu.getMenuType())) {
            return UserConstants.LAYOUT;
        }
        //菜单
        return menu.getComponent();
    }

    /**
     * 获取路由路径
     *
     * @param menu 菜单实体
     * @return 路由路径
     */
    private String getRouterPath(SysMenu menu) {
        if (menu.getParentId() == 0 && UserConstants.TYPE_DIR.equals(menu.getMenuType())) {
            return "/" + menu.getPath();
        }
        return menu.getPath();

    }

    /**
     * 登出接口
     * 同时删除缓存
     */
    @Override
    public void logout() {
        //获取用户uuid
        String uuid = SecurityUtils.getLoginUser().getUuid();
        //删除缓存
        tokenService.deleteToken(uuid);
    }
}
