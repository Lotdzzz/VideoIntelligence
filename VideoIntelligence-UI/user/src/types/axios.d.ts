import 'axios'

declare module 'axios' {
    export interface AxiosRequestConfig {
        /**
         * 是否在请求头注入 token，默认注入
         * 白名单接口（如第三方登录）传 false
         */
        isToken?: boolean
    }
}
