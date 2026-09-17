import { defineStore } from 'pinia'
import { ref } from 'vue'
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

  return { userInfo, isLogin, setUserInfo, clearUserInfo }
})
