const TOKEN_KEY = 'token'

//获取token
export function getToken() {
    return localStorage.getItem(TOKEN_KEY)
}

//设置token
export function setToken(token: string) {
    localStorage.setItem(TOKEN_KEY, token)
}

//移除token
export function removeToken() {
    localStorage.removeItem(TOKEN_KEY)
}