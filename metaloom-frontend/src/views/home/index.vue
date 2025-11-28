<template>
  <div class="home-page">
    <AppHeader />
    
    <main class="main-content">
      <div class="hero-section">
        <h1 class="hero-title">欢迎来到元织智能体</h1>
        <p class="hero-subtitle">探索技术，创造价值</p>
      </div>
      
      <div class="cards-grid">
        <NavigationCard
          v-for="card in navigationCards"
          :key="card.name"
          :title="card.title"
          :description="card.description"
          :icon="card.icon"
          :route="card.route"
          :status="card.status"
          @click="handleCardClick"
        />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue';
import { useRouter } from 'vue-router';
import AppHeader from '@/components/layout/Header/index.vue';
import NavigationCard from '@/components/common/NavigationCard.vue';

interface CardItem {
  name: string;
  title: string;
  description: string;
  icon: string;
  route: string;
  status: 'active' | 'developing';
}

const router = useRouter();

const navigationCards = reactive<CardItem[]>([
  {
    name: 'english',
    title: '英语学习',
    description: '智能英语学习助手',
    icon: '📚',
    route: '/english',
    status: 'active'
  },
  {
    name: 'wealth',
    title: '财富管理',
    description: '个人财务规划工具',
    icon: '💰',
    route: '/wealth',
    status: 'active'
  },
  {
    name: 'agent',
    title: 'AI助手',
    description: '智能对话助手',
    icon: '🤖',
    route: '/agent',
    status: 'active'
  },
  {
    name: 'extension',
    title: '扩展功能',
    description: '敬请期待...',
    icon: '🚀',
    route: '',
    status: 'developing'
  }
]);

const handleCardClick = (route: string, status: string) => {
  if (status === 'active' && route) {
    router.push(route);
  }
};
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.main-content {
  padding-top: 64px;
  max-width: 1200px;
  margin: 0 auto;
  padding-left: 24px;
  padding-right: 24px;
}

.hero-section {
  text-align: center;
  padding: 80px 0 60px;
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 16px;
  letter-spacing: -1px;
}

.hero-subtitle {
  font-size: 20px;
  color: #666;
  margin: 0;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  padding-bottom: 80px;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 32px;
  }
  
  .hero-subtitle {
    font-size: 16px;
  }
  
  .cards-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}
</style>
