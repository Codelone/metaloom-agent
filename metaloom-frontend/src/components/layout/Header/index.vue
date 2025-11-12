<template>
  <header class="app-header">
    <div class="container">
      <div class="left">
        <span class="brand" @click="goHome">Metaloom Agent</span>
      </div>
      <div class="right">
        <slot />
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();
function goHome() {
  router.push('/');
}
</script>

<style scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 64px;
  background: #ffffff;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  z-index: 1000;
}

.container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.brand {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  cursor: pointer;
}

.right {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style> 