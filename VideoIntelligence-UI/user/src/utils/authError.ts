/**
 * 登录相关错误的提示文案解析
 *
 * 约定：后端异常统一由 GlobalExceptionHandler 返回 Result.error(e.getMessage())，
 * 即 HTTP 200 + { code: 500, msg: "..." }，前端一律「原样展示后端 msg」，
 * 不做错误码映射、不做前缀裁剪、不做文案美化。
 *
 * 只有后端确实没有给出 msg 时才退化为统一兜底提示，以下三类都没有 msg：
 * 1. 401：AuthenticationEntryPointImpl 返回固定响应体 {"code": "401"}
 * 2. 未被 GlobalExceptionHandler 覆盖的异常（第三方接口失败、NPE 等）：返回 Spring 默认错误结构
 * 3. 纯网络异常（Network Error）：没有响应体
 */

/** 后端未提供 msg 时的统一兜底提示 */
export const AUTH_ERROR_FALLBACK_MESSAGE = '登录失败，请稍后重试'

/**
 * 读取后端返回的提示文案
 *
 * @param error 任意错误（request.ts 透传的 Result 响应体、axios 错误等）
 * @returns 后端 msg 原样文案；后端没有 msg 时返回统一兜底提示
 */
export function resolveAuthErrorMessage(error: unknown): string {
    // 统一响应体 Result：{ code, msg, data }，msg 由后端异常决定，原样返回
    if (error && typeof error === 'object') {
        const detail = error as Record<string, unknown>
        if (typeof detail.msg === 'string' && detail.msg !== '') {
            return detail.msg
        }
    }
    return AUTH_ERROR_FALLBACK_MESSAGE
}
