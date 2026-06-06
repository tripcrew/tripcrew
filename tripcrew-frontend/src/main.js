import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

import './assets/styles/reset.css'
import './assets/styles/tokens.css'
import './assets/styles/global.css'

const app = createApp(App)
app.use(createPinia()) // router 보다 먼저 등록(라우터 가드에서 store 사용 가능)
app.use(router)
app.mount('#app')
