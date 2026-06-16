/**
 * @file main.ts
 * @description Vue 应用入口文件，负责初始化 Vue 实例、注册插件（Pinia 状态管理、路由），
 *              并在挂载前恢复用户会话状态
 * @input 无
 * @output 挂载到 DOM 的 Vue 应用实例
 */

// 引入全局样式文件
import './index.css'
// 引入 FontAwesome 图标库样式
import '@fortawesome/fontawesome-free/css/all.min.css';

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { useAccountStore } from './stores/network/accountStore'
import { useShoppingCartStore } from './stores/shoppingCartStore'

// 创建 Vue 应用实例
const app = createApp(App)

// 创建 Pinia 状态管理实例并注册到应用
const pinia = createPinia()
app.use(pinia)

// 在应用挂载前恢复用户会话，确保页面刷新后登录状态不丢失
// 使用async/await等待会话恢复完成，避免令牌过期导致的"假登录"状态
const accountStore = useAccountStore()
accountStore.restoreSession().then(() => {
  // 注册路由插件
  app.use(router)

  // 将应用挂载到 HTML 中 id 为 app 的 DOM 元素
  app.mount('#app')
}).catch(() => {
  // 会话恢复失败（令牌无效或过期），清空购物车
  // 防止未登录用户刷新页面后仍能看到上次购物车数据
  const shoppingCartStore = useShoppingCartStore()
  shoppingCartStore.clearCart()

  // 即使会话恢复失败，仍需挂载应用
  app.use(router)
  app.mount('#app')
})
