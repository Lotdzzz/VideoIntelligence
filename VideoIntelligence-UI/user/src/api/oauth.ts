import request from '@/utils/request'

/**
 * 获取 GitHub 授权登录地址
 * 后端根据配置构造 github 授权 url 并返回，由前端负责整页跳转
 *
 * @returns github 授权 url（其中包含后端生成的 state）
 */
export function getGithubLoginUrl() {
    // 白名单接口，不需要携带 token
    return request.post<unknown, string>('/auth/oauth/github/login', null, {
        headers: { isToken: false },
    })
}

/**
 * GitHub 授权回调换令牌
 * 前端回调页把 github 返回的 code/state 交回后端，由后端换取 JWT
 *
 * @param params github 回调返回的授权码与 state
 * @returns 后端生成的 jwt
 */
export function githubCallback(params: { code: string; state: string }) {
    // 白名单接口，不需要携带 token
    return request.get<unknown, string>('/auth/oauth/github/callback', {
        params,
        headers: { isToken: false },
    })
}
