<template>
  <div class="app-layout">
    <SidebarHistory />
    <ChatPanel />
  </div>
</template>

<script setup lang="ts">
import SidebarHistory from '@/components/SidebarHistory.vue';
import ChatPanel from '@/components/ChatPanel.vue';
</script>

<style scoped>
.app-layout {
  display: flex;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}
</style>