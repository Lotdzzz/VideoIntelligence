export const routesConstants = {
    //首页
    HOME: '/index',
    //获取当前登录用户信息
    GET_USER_INFO: '/auth/getUserInfo',
    //静态文件（头像等）访问前缀
    UPLOAD: '/upload',
}

export const routesIndexConstants = {
    //登录
    LOGIN: '/login',
    //GitHub等第三方授权回调页（需与后端redirect-uri保持一致）
    OAUTH_CALLBACK: '/oauth/callback',
    //404
    NOT_FOUND: '/404',
    //401
    UNAUTHORIZED: '/401',
}
