import { defineStore } from 'pinia'
import { ref } from 'vue'

export const usePermissionsStore = defineStore('permissions', () => {
    /** 权限列表 */
    const permissions = ref<string[]>([])

    /** 设置权限列表 */
    function setPermissions(perms: string[]) {
        permissions.value = perms
    }

    /** 清空权限列表 */
    function clearPermissions() {
        permissions.value = []
    }

    return { permissions, setPermissions, clearPermissions }
})