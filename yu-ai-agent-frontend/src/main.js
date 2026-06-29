import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from 'axios'
import './assets/styles.css'

axios.defaults.baseURL = 'http://localhost:8123/api'

const app = createApp(App)
app.use(router)
app.mount('#app')
