import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useRolesStore = defineStore('roles', () => {
    /** 角色列表 */
    const roles = ref<string[]>([])

    /** 设置角色列表 */
    function setRoles(roleList: string[]) {
        roles.value = roleList
    }

    /** 清空角色列表 */
    function clearRoles() {
        roles.value = []
    }

    return { roles, setRoles, clearRoles }
})