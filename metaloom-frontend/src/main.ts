import { createApp } from 'vue';
import App from '@/App.vue';
import { createPinia } from 'pinia';
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';
import router from '@/router';

// 全局样式
import 'element-plus/dist/index.css'

// 创建Vue应用实例
const app = createApp(App);

// 创建Pinia状态管理
const pinia = createPinia();
pinia.use(piniaPluginPersistedstate);

// 注册插件
app.use(pinia);
app.use(router);

// 挂载应用
app.mount('#app');