export const routesConstants = {
    // 登录页
    LOGIN: '/auth/login',
    // 获取用户信息
    GET_USER_INFO: '/auth/getUserInfo',
    // 获取路由信息（权限列表 角色列表 路由树）
    GET_VALID: '/auth/getValid',
    // 文件上传
    UPLOAD: '/upload',
    // 退出登录
    LOGOUT: '/auth/logout',
    // 用户管理基础路径
    USER_BASE: '/auth/user',
    // 分页查询用户
    USER_LIST: '/auth/user/list',
    // 根据用户ID查询用户
    USER_GET_BY_ID: '/auth/user',
    // 新增用户
    USER_ADD: '/auth/user',
    // 修改用户
    USER_UPDATE: '/auth/user',
    // 删除用户
    USER_DELETE: '/auth/user',
    // 批量删除用户
    USER_BATCH_DELETE: '/auth/user/batchDelete',
    // 根据用户ID查询本人用户（归属校验）
    USER_GET_BY_OWNER: '/auth/user/getByOwner',
    // 修改本人用户（归属校验）
    USER_UPDATE_BY_OWNER: '/auth/user/updateByOwner',
    // 分页查询角色
    ROLE_LIST: '/auth/role/list',
    // 根据角色ID查询角色
    ROLE_GET_BY_ID: '/auth/role',
    // 新增角色
    ROLE_ADD: '/auth/role',
    // 修改角色
    ROLE_UPDATE: '/auth/role',
    // 删除角色
    ROLE_DELETE: '/auth/role',
    // 批量删除角色
    ROLE_BATCH_DELETE: '/auth/role/batchDelete',
    // 查询所有角色列表
    ROLE_LIST_ALL: '/auth/role/get/all',
    // 分页查询菜单
    MENU_LIST: '/auth/menu/list',
    // 根据菜单ID查询菜单
    MENU_GET_BY_ID: '/auth/menu',
    // 新增菜单
    MENU_ADD: '/auth/menu',
    // 修改菜单
    MENU_UPDATE: '/auth/menu',
    // 删除菜单
    MENU_DELETE: '/auth/menu',
    // 批量删除菜单
    MENU_BATCH_DELETE: '/auth/menu/batchDelete',
    // AI提示词管理基础路径
    AI_PROMPT_BASE: '/auth/ai/prompt',
    // 分页查询AI提示词
    AI_PROMPT_LIST: '/auth/ai/prompt/list',
    // 根据ID查询AI提示词
    AI_PROMPT_GET_BY_ID: '/auth/ai/prompt',
    // 新增AI提示词
    AI_PROMPT_ADD: '/auth/ai/prompt',
    // 修改AI提示词
    AI_PROMPT_UPDATE: '/auth/ai/prompt',
    // 删除AI提示词
    AI_PROMPT_DELETE: '/auth/ai/prompt',
    // 批量删除AI提示词
    AI_PROMPT_BATCH_DELETE: '/auth/ai/prompt/batchDelete',
    // AI对话接口
    MCP_CHAT: '/auth/mcp/chat',
    // 认证登录日志管理基础路径
    AUTH_LOG_BASE: '/certification/monitor/authLog',
    // 分页查询认证登录日志
    AUTH_LOG_LIST: '/certification/monitor/authLog/list',
    // 根据日志ID查询认证登录日志
    AUTH_LOG_GET_BY_ID: '/certification/monitor/authLog',
    // 删除认证登录日志
    AUTH_LOG_DELETE: '/certification/monitor/authLog',
    // 批量删除认证登录日志
    AUTH_LOG_BATCH_DELETE: '/certification/monitor/authLog/batchDelete',
    // 系统监控信息
    SYSTEM_MONITOR_INFO: '/os/monitor/info',
}

export const routesIndexConstants = {
    // 登录页
    LOGIN: '/login',
    //首页
    DASHBOARD: '/dashboard',
    // 个人中心
    PROFILE: '/system/user/profile',
    //404
    NOT_FOUND: '/404',
    //401
    UNAUTHORIZED: '/401',
}
