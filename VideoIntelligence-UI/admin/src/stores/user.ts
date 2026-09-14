import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { SysUser } from '@/types/user/user'

export const useUserStore = defineStore('user', () => {
    const userInfo = ref<SysUser>({
        userId: -1,
        userName: '',
        nickName: '',
        email: '',
        avatar: '',
        status: '',
        delFlag: '',
        loginIp: '',
        loginDate: '',
        pwdUpdateDate: '',
        createBy: '',
        createTime: '',
        updateBy: '',
        updateTime: '',
        remark: '',
    })

    /** 角色列表 */
    const roleList = ref<string[]>([])

    /** 权限列表 */
    const permissions = ref<string[]>([])

    /** 设置用户信息 */
    function setUserInfo(info: Partial<SysUser>) {
        userInfo.value = { ...userInfo.value, ...info }
    }

    /** 设置角色列表 */
    function setRoleList(roles: string[]) {
        roleList.value = roles
    }

    /** 设置权限列表 */
    function setPermissions(perms: string[]) {
        permissions.value = perms
    }

    /** 清除用户信息 */
    function clearUserInfo() {
        userInfo.value = {
            userId: 0,
            userName: '',
            nickName: '',
            email: '',
            avatar: '',
            status: '',
            delFlag: '',
            loginIp: '',
            loginDate: '',
            pwdUpdateDate: '',
            createBy: '',
            createTime: '',
            updateBy: '',
            updateTime: '',
            remark: '',
        }
        roleList.value = []
        permissions.value = []
    }

    return { userInfo, roleList, permissions, setUserInfo, setRoleList, setPermissions, clearUserInfo }
})