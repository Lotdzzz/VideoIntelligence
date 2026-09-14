import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

// 引入ElementPlus核心
import ElementPlus from 'element-plus'
// 必须引入全部样式
import 'element-plus/dist/index.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus, {
  size: 'default', // 设置默认的组件大小
})

app.mount('#app')
