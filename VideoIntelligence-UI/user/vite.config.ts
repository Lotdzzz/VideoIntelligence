import {fileURLToPath, URL} from 'node:url'

import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import {constants} from './src/constants/configuration'

// https://vite.dev/config/
export default defineConfig({
    plugins: [
        vue(),
        vueDevTools(),
    ],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url)),
        },
    },
    server: {
        //前端端口
        port: constants.FRONTEND_PORT,
        // 固定为 127.0.0.1：GitHub 回调地址使用 127.0.0.1，避免与 localhost 形成不同源导致本地存储不互通
        host: '127.0.0.1',
        open: true,
        //跨域问题配置
        proxy: {
            [constants.API_BASE_URL]: {
                target: constants.GATEWAY_URL,
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api/, ''),
            },
        },
    },
})
