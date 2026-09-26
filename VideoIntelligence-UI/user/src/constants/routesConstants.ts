export const routesConstants = {
    //首页
    HOME: '/index',
    //视频页（用户端侧边栏「视频」入口）
    VIDEO: '/video',
    //获取当前登录用户信息
    GET_USER_INFO: '/auth/getUserInfo',
    //静态文件（头像等）访问前缀
    UPLOAD: '/upload',
    //查询当前登录用户的资源文件（视频）分类列表
    FILE_CATEGORY_USER: '/file/user/category/user',
    //搜索资源文件（视频）分类（分页接口，支持 userId / categoryName 等条件）
    FILE_CATEGORY_LIST: '/file/user/category/list',
    //查询资源文件（视频）分类列表（不分页，用于全量删除前取全部ID）
    FILE_CATEGORY_ALL: '/file/user/category/all',
    //修改资源文件（视频）分类
    FILE_CATEGORY_UPDATE: '/file/user/category',
    //新增资源文件（视频）分类
    FILE_CATEGORY_ADD: '/file/user/category',
    //删除单个资源文件（视频）分类（后拼 /{id}）
    FILE_CATEGORY_DELETE: '/file/user/category',
    //批量删除资源文件（视频）分类（全量删除复用该接口）
    FILE_CATEGORY_BATCH_DELETE: '/file/user/category/batchDelete',
    //分页查询资源文件（视频）列表（支持 categoryId / userId / originalName / status / 创建时间范围等条件）
    FILE_RESOURCE_LIST: '/file/user/resource/list',
    //删除单个资源文件（视频）（后拼 /{id}）
    FILE_RESOURCE_DELETE: '/file/user/resource',
    //批量删除资源文件（视频）（body 为 ID 集合）
    FILE_RESOURCE_BATCH_DELETE: '/file/user/resource/batchDelete',
    //退出登录接口
    LOGOUT: '/auth/logout',
    //获取视频分片上传任务（返回 uploadId / 分片大小 / 总分片数 / 各分片预签名URL）
    FILE_PREDESIGN_SLICE_INFO: '/file/user/predesign/slice/info',
    //保存视频封面图片（返回封面访问URL）
    FILE_PREDESIGN_COVER_SAVE: '/file/user/predesign/cover/save',
    //上报某个分片上传完成（携带 ETag；最后一片上报后由服务端合并分片并写入资源库）
    FILE_PREDESIGN_SLICE_COMPLETE: '/file/user/predesign/slice/upload',
    //解析外部视频链接并返回可选择的视频条目（演示，不直接落库）
    FILE_LINK_RECEIVE: '/file/link/receive',
}

/**
 * 侧边栏「视频」分类菜单项 index 前缀
 * 分类项 index 形如 video-category-3，选中后内容区按 categoryId=3 加载资源
 * 统一放在这里维护，避免 layout 与视频页两处硬编码字符串
 */
export const VIDEO_CATEGORY_MENU_PREFIX = 'video-category-'

/** 侧边栏「全部视频」菜单项 index：选中后清掉 URL 上的分类ID，内容区展示全部资源 */
export const VIDEO_CATEGORY_ALL_INDEX = 'video-category-all'

export const routesIndexConstants = {
    //登录
    LOGIN: '/login',
    //GitHub 授权回调页（需与后端redirect-uri保持一致）
    OAUTH_CALLBACK: '/oauth/callback',
    //404
    NOT_FOUND: '/404',
    //401
    UNAUTHORIZED: '/401',
}
