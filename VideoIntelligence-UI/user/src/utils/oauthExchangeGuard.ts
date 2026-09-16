/**
 * GitHub 授权码兑换的一次性闸门
 *
 * 说明：授权码（code）在 github 侧是一次性的，重复兑换会被拒绝；
 * 且后端在兑换开始前就会消费掉 redis 中的 state，重复请求只会白打后端。
 *
 * 注意：这里必须放在真正的模块作用域（而组件 <script setup> 内的变量是"每个组件实例"一份，
 * 页面刷新 / 重新挂载 / 标签恢复 / 多标签 / HMR 都会重置，起不到去重作用），
 * 因此标记同时写入 sessionStorage，保证跨加载也能拦截重复兑换。
 */

/** 兑换标记在 sessionStorage 中的键前缀 */
const EXCHANGE_KEY_PREFIX = 'oauth_exchange:'

/**
 * 是否已经为该 state 发起过兑换
 *
 * @param state 后端下发的 state
 * @returns true 表示已经兑换过，不应再次请求后端
 */
export function hasExchangeStarted(state: string): boolean {
    if (!state) {
        return false
    }
    return sessionStorage.getItem(EXCHANGE_KEY_PREFIX + state) !== null
}

/**
 * 标记该 state 已发起兑换（必须在请求后端之前调用）
 *
 * @param state 后端下发的 state
 */
export function markExchangeStarted(state: string): void {
    if (!state) {
        return
    }
    sessionStorage.setItem(EXCHANGE_KEY_PREFIX + state, String(Date.now()))
}
