import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { RouterVO } from '@/types/routers'

/**
 * 动态路由（菜单）仓库
 * 保存后端返回的路由树，供侧边栏菜单渲染使用
 */
export const useRoutersStore = defineStore('routers', () => {
    /** 路由树 */
    const routers = ref<RouterVO[]>([])

    /** 设置路由树 */
    function setRouters(list: RouterVO[]) {
        routers.value = list
    }

    /** 清空路由树 */
    function clearRouters() {
        routers.value = []
    }

    return { routers, setRouters, clearRouters }
})