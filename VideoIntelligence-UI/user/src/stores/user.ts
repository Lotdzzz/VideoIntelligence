import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserInfo } from '@/api/user'
import { getToken, removeToken } from '@/utils/token'
import type { UserInfo } from '@/types/user/userInfo'

/**
 * 未登录时的空用户信息
 * 用零值对象代替 null，模板与业务代码读取字段时无需判空
 */
function createEmptyUserInfo(): UserInfo {
  return {
    userId: 0,
    userName: '',
    nickName: '',
    email: '',
    avatar: '',
  }
}

/**
 * 全局用户信息 store
 * 登录成功（或刷新后 token 校验通过）时写入，退出登录 / token 失效时清空，
 * 左下角用户区、后续需要的页面统一从这里读取
 */
export const useUserStore = defineStore('user', () => {
  /** 当前用户信息 */
  const userInfo = ref<UserInfo>(createEmptyUserInfo())

  /** 是否已登录 */
  const isLogin = ref(false)

  /** 是否正在用本地 token 恢复登录态（首屏渲染用，避免刷新页面时先闪一下「游客」） */
  const restoring = ref(false)

  /**
   * 进行中的恢复请求
   * /index 与 /video 是两个路由记录共用同一个 Layout，布局重新挂载会再次调用 restoreLogin，
   * 这里缓存同一个 Promise，保证一次页面生命周期内 /auth/getUserInfo 只请求一次
   */
  let restorePromise: Promise<boolean> | null = null

  /** 设置用户信息（拉取到 /auth/getUserInfo 数据后调用） */
  function setUserInfo(info: Partial<UserInfo>) {
    userInfo.value = { ...createEmptyUserInfo(), ...info }
    isLogin.value = true
  }

  /** 清除用户信息（退出登录 / token 失效时调用） */
  function clearUserInfo() {
    userInfo.value = createEmptyUserInfo()
    isLogin.value = false
  }

  /** 清除本地登录凭证与登录态（退出登录 / token 失效时调用） */
  function clearLoginState() {
    removeToken()
    clearUserInfo()
  }

  /**
   * 真正发起恢复请求
   * 失败时清空本地凭证（token 已失效，留着只会让后续请求继续 401）并清空请求缓存，
   * 便于用户重新登录后再恢复；错误继续抛给调用方，由调用方决定如何提示
   */
  async function requestLoginUser(): Promise<boolean> {
    try {
      const info = (await getUserInfo()) as UserInfo
      setUserInfo(info)
      return true
    } catch (error) {
      restorePromise = null
      clearLoginState()
      throw error
    }
  }

  /**
   * 用本地 token 恢复登录态（页面打开 / 刷新时调用）
   * 1）已登录：直接复用当前登录态，不重复请求
   * 2）无 token：按游客处理，不发请求
   * 3）有 token：请求 /auth/getUserInfo，成功后写入 store 并置为登录态；并发 / 重复调用共享同一个请求
   *
   * @returns 是否恢复为登录态
   */
  function restoreLogin(): Promise<boolean> {
    if (isLogin.value) {
      return Promise.resolve(true)
    }
    if (!getToken()) {
      clearUserInfo()
      return Promise.resolve(false)
    }
    if (!restorePromise) {
      restoring.value = true
      restorePromise = requestLoginUser().finally(() => {
        restoring.value = false
      })
    }
    return restorePromise
  }

  return {
    userInfo,
    isLogin,
    restoring,
    setUserInfo,
    clearUserInfo,
    clearLoginState,
    restoreLogin,
  }
})
