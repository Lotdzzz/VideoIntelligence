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
            // 首页：带布局
            path: routesConstants.HOME,
            component: Layout,
        },
        {
            // GitHub 等第三方登录的授权回调页：无布局，独立整页展示
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