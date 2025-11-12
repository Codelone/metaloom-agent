import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/agent/analysis'
  },
  {
    path: '/agent',
    children: [
      {
        path: 'analysis',
        name: 'AgentAnalysis',
        component: () => import('@/views/agent/analysis.vue')
      },
      {
        path: '',
        name: 'AgentHome',
        component: () => import('@/views/agent/index.vue')
      }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(import.meta.env.VITE_APP_BASE_PATH || '/metaloom/'),
  routes
});

export default router; 