import {createRouter, createWebHistory} from 'vue-router'
import {routesConstants, routesIndexConstants} from '@/constants/routesConstants'

import Layout from '@/layout/index.vue'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            // 根路径重定向到首页
            path: '/',
            redirect: routesConstants.HOME,
        },
        {
            // 首页：带布局，页面内容渲染到 Layout 右侧内容区的 <router-view />
            path: routesConstants.HOME,
            component: Layout,
            children: [
                {
                    // 空路径子路由：精确匹配 /index 时渲染首页内容（与 admin 端约定一致）
                    path: '',
                    name: 'Home',
                    component: () => import('@/views/index.vue'),
                    // title 为面包屑展示名称（与侧边栏菜单名保持一致）
                    meta: { title: '首页' },
                },
            ],
        },
        {
            // 视频：与首页共用布局，页面内容渲染到 Layout 右侧内容区的 <router-view />
            path: routesConstants.VIDEO,
            component: Layout,
            children: [
                {
                    // 空路径子路由：精确匹配 /video 时渲染视频页
                    path: '',
                    name: 'Video',
                    component: () => import('@/views/video/index.vue'),
                    meta: { title: '视频' },
                },
            ],
        },
        {
            // GitHub 授权回调页：无布局，独立整页展示
            path: routesIndexConstants.OAUTH_CALLBACK,
            name: 'OauthCallback',
            component: () => import('@/views/oauth/GithubCallback.vue'),
        },
        {
            path: routesIndexConstants.UNAUTHORIZED,
            name: 'Unauthorized',
            component: () => import('@/views/error/401.vue'),
        },
        {
            path: routesIndexConstants.NOT_FOUND,
            name: 'NotFound',
            component: () => import('@/views/error/404.vue'),
        },
        {
            path: '/:pathMatch(.*)*',
            redirect: routesIndexConstants.NOT_FOUND,
        },
    ],
})

export default router