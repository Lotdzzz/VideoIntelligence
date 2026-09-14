import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getToken, setToken, removeToken } from '@/utils/token'

export const useTokenStore = defineStore('token', () => {
    const token = ref<string>(getToken() || '')

    /** 保存 token */
    function saveToken(value: string) {
        token.value = value
        setToken(value)
    }

    /** 清除 token */
    function clearToken() {
        token.value = ''
        removeToken()
    }

    return { token, saveToken, clearToken }
})