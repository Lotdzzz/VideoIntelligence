package com.framework.constants;

/**
 * @author dotm
 */
public class ExceptionConstants {
    /**
     * 用户异常信息
     */
    public static final String FETCH_USERID_EXCEPTION = "获取用户ID异常";

    public static final String FETCH_USER_NAME_EXCEPTION = "获取用户账户异常";

    public static final String FETCH_USER_INFO_EXCEPTION = "获取用户信息异常";

    public static final String USER_NOT_EXISTS = "用户不存在";

    public static final String USERNAME_PASSWORD_NOT_MATCH = "用户名或密码错误";

    public static final String USER_LOCKED = "用户已锁定";

    public static final String USER_ACCOUNT_EXPIRED = "用户账号已过期";

    /**
     * 登录异常
     */
    public static final String LOGIN_ERROR = "登录异常";

    public static final String LOGIN_CSRF_ERROR = "伪造请求异常";

    public static final String LOGIN_EXPIRE_OUT = "用户认证过期，请重新登录";

    public static final String LOGIN_AUTH_ERROR = "用户认证异常，请重新登录";

    public static final String LOGIN_TIME_OUT = "登录超时，请重新登录";

    public static final String LOGIN_NET_ERROR = "网络异常，无法访问认证服务";

    public static final String LOGIN_AUTH_FAIL = "登录认证失败";

    public static final String LOGIN_AUTH_SERVICE_UNAVAILABLE = "登录认证服务暂不可用";

    public static final String LOGIN_AUTH_REQUEST_ERROR = "认证请求失败";


    /**
     * 角色异常信息
     */
    public static final String ROLE_NOT_EXISTS = "角色列表为空";

    public static final String ROLE_BIND_MENUS = "角色绑定菜单异常";

    /**
     * 网关异常
     */

    public static final String TOKEN_INVALID = "令牌不存在或已失效";

    public static final String SERVICE_NOT_FOUND = "服务未找到";

    public static final String SERVER_ERROR = "内部服务器错误";

    public static final String LOGIN_ADDR_ERROR = "令牌设备或地点异常";

    public static final String LOGIN_IP_ERROR = "令牌IP异常";

    /**
     * 异常模块
     */

    public static final String USER = "用户错误：";

    public static final String GATEWAY = "网关错误：";

    public static final String ROLE = "角色错误：";

    /**
     * 文件上传模块
     */
    public static final String FILE_UPLOAD = "file_upload";

    public static final String FILE_NULL = "文件为空";

    public static final String FILE_NAME_INVALID = "文件名不合法";

    public static final String FILE_PRE_SIGN_ERROR = "文件上传预签名异常";

    /**
     * xss攻击异常
     */
    public static final String XSS_ATTACK = "非法参数";

    /**
     * 未授权异常
     */
    public static final String UNAUTHORIZED = "未授权";
}
