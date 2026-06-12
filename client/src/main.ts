import './index.css'
import '@fortawesome/fontawesome-free/css/all.min.css';

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { useAccountStore } from './stores/network/accountStore'

const app = createApp(App)

const pinia = createPinia()
app.use(pinia)

// Restore session before mounting the app
const accountStore = useAccountStore()
accountStore.restoreSession()

app.use(router)

app.mount('#app')
