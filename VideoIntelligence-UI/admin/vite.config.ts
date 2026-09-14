import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import { constants } from './src/constants/configuration.ts'

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
    open: true,
    //跨域问题配置
    proxy: {
      [constants.API_BASE_URL] : {
        target: constants.GATEWAY_URL,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
      // 静态文件上传目录：/upload 直接代理到网关，不做前缀重写
      '/upload': {
        target: constants.GATEWAY_URL,
        changeOrigin: true,
      },
    },
  },
})
