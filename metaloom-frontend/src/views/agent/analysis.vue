<template>
  <div class="agent-analysis-page">
    <AppHeader />
    <main class="main-content">
      <div class="page-header">
        <el-button @click="goBack" :icon="ArrowLeft">返回首页</el-button>
        <h1 class="page-title">数据分析 - 流式演示</h1>
      </div>

      <el-form :inline="true" @submit.prevent>
        <el-form-item label="问题">
          <el-input
            v-model="query"
            placeholder="请输入分析问题"
            style="width: 560px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="startStream">开始分析</el-button>
        </el-form-item>
      </el-form>

      <div class="groups">
        <el-card v-for="(g, idx) in groups" :key="idx" class="group-card">
          <div class="group-header">
            <span class="group-title">{{ g.request || `步骤 ${idx + 1}` }}</span>
            <span class="group-status" v-if="!g.isDone">思考中{{ g.waitDots }}</span>
          </div>
          <pre class="group-content">{{ g.displayContent }}</pre>
        </el-card>
      </div>

      <div class="final-result" v-if="finalAnswer">
        <h2>最终答案</h2>
        <el-card class="answer-card">
          <pre class="answer-text">{{ finalAnswer }}</pre>
        </el-card>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowLeft } from '@element-plus/icons-vue';
import AppHeader from '@/components/layout/Header/index.vue';
import { streamAnalysis, type StepEvent } from '@/api/analysis';

const router = useRouter();
const baseUrl = '';

const query = ref('');
const loading = ref(false);

interface GroupItem {
  request: string;
  content: string;
  displayContent: string;
  isDone: boolean;
  typingTimer?: number;
  waitDots: string;
  waitTimer?: number;
  contentQueue: string[]; // 内容队列，按顺序添加和显示
  currentIndex: number; // 当前显示到的字符位置
  isTyping: boolean; // 是否正在打字
}

const groups = ref<GroupItem[]>([]);
const finalAnswer = ref('');

const goBack = () => router.push('/');

function formatJSON(obj: unknown): string {
  try {
    return JSON.stringify(obj, null, 2);
  } catch {
    return String(obj);
  }
}

function getOrCreateGroup(requestKey: string | undefined): GroupItem {
  const key = requestKey || `未命名步骤_${Date.now()}`;
  let g = groups.value.find(x => x.request === key);
  if (!g) {
    // console.log('创建新组:', key);
    g = { 
      request: key, 
      content: '', 
      displayContent: '', 
      isDone: false, 
      waitDots: '', 
      contentQueue: [],
      currentIndex: 0,
      isTyping: false
    };
    groups.value.push(g);
    startWaitingDots(g);
  }
  return g;
}

function startWaitingDots(group: GroupItem) {
  if (group.waitTimer) return;
  group.waitTimer = window.setInterval(() => {
    if (group.isDone) {
      if (group.waitTimer) window.clearInterval(group.waitTimer);
      group.waitTimer = undefined;
      group.waitDots = '';
      return;
    }
    group.waitDots = group.waitDots.length >= 3 ? '' : group.waitDots + '.';
  }, 500);
}

function startTyping(group: GroupItem) {
  if (group.isTyping) {
    // console.log('组', group.request, '已在打字中，跳过');
    return;
  }
  
  // console.log('开始打字组:', group.request);
  group.isTyping = true;
  
  const typeNextChar = () => {
    // 先检查是否被停止
    if (!group.isTyping) {
      return;
    }
    
    // 处理队列中的所有新内容
    let hasNewContent = false;
    while (group.contentQueue.length > 0) {
      const newContent = group.contentQueue.shift()!;
      group.content += newContent;
      hasNewContent = true;
    }
    
    // 逐字符显示
    if (group.currentIndex < group.content.length) {
      group.displayContent = group.content.substring(0, group.currentIndex + 1);
      group.currentIndex++;
      
      // 继续下一个字符，如果有新内容则加快速度
      const delay = hasNewContent ? 8 : 16;
      group.typingTimer = window.setTimeout(typeNextChar, delay);
    } else {
      // 当前内容显示完成
      if (group.contentQueue.length > 0) {
        // 还有新内容，立即继续处理
        group.typingTimer = window.setTimeout(typeNextChar, 0);
      } else if (group.isDone) {
        // 已完成且无新内容，停止打字效果
        // console.log('组', group.request, '打字完成');
        group.isTyping = false;
      } else {
        // 等待新内容，50ms后重新检查
        group.typingTimer = window.setTimeout(typeNextChar, 50);
      }
    }
  };
  
  // 立即开始
  typeNextChar();
}

async function startStream() {
  if (!query.value) return;
  loading.value = true;
  
  // 清理之前的定时器
  groups.value.forEach(group => {
    if (group.typingTimer) {
      window.clearTimeout(group.typingTimer);
    }
    if (group.waitTimer) {
      window.clearInterval(group.waitTimer);
    }
  });
  
  groups.value = [];
  finalAnswer.value = '';

  await streamAnalysis(baseUrl, { query: query.value }, (evt) => {
    // console.log('收到事件:', evt.type, evt.request, typeof evt.result === 'string' ? evt.result.substring(0, 50) + '...' : evt.result);
    
    if (evt.type === 'final_answer') {
      const text = typeof evt.result === 'string' ? evt.result : formatJSON(evt.result);
      finalAnswer.value = text;
      loading.value = false;
      return;
    }

    const group = getOrCreateGroup(evt.request);
    if (evt.type === 'group_done') {
      // console.log('组完成:', group.request);
      group.isDone = true;
      // 如果当前组没在打字，需要重新启动以完成剩余内容的显示
      if (!group.isTyping && (group.contentQueue.length > 0 || group.currentIndex < group.content.length)) {
        startTyping(group);
      }
      return;
    }

    const piece = typeof evt.result === 'string' ? evt.result : formatJSON(evt.result);
    
    // 将新内容加入队列
    group.contentQueue.push(piece);
    // console.log('添加内容到组:', group.request, '队列长度:', group.contentQueue.length, '正在打字:', group.isTyping);
    
    // 如果没有在打字，开始打字效果
    if (!group.isTyping) {
      startTyping(group);
    }
  }, (err) => {
    console.error(err);
    loading.value = false;
  });

  loading.value = false;
}

// 组件销毁时清理定时器
onBeforeUnmount(() => {
  groups.value.forEach(group => {
    if (group.typingTimer) {
      window.clearTimeout(group.typingTimer);
    }
    if (group.waitTimer) {
      window.clearInterval(group.waitTimer);
    }
  });
});
</script>

<style scoped>
.agent-analysis-page {
  min-height: 100vh;
  background: #f8f9fa;
}

.main-content {
  padding-top: 64px;
  max-width: 1200px;
  margin: 0 auto;
  padding-left: 24px;
  padding-right: 24px;
}

.page-header {
  padding: 24px 0 16px 0;
  border-bottom: 1px solid #eee;
  margin-bottom: 16px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 8px 0 0 0;
}

.groups {
  margin-top: 16px;
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.group-card {
  background: #fff;
}

.group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.group-title {
  font-weight: 600;
  color: #1a1a1a;
}

.group-status {
  font-style: italic;
}

.group-content {
  white-space: pre-wrap;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  font-size: 13px;
  line-height: 1.7;
}

.final-result {
  margin-top: 24px;
}

.answer-card {
  background: #fff;
}

.answer-text {
  white-space: pre-wrap;
}
</style> 