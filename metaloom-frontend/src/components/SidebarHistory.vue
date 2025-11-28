<template>
  <div class="sidebar">
    <div class="sidebar-header">
      <el-button type="primary" class="new-chat-btn" @click="createNewSession">
        <el-icon><Plus /></el-icon> New Chat
      </el-button>
    </div>

    <div class="search-box">
      <el-input
        v-model="searchQuery"
        placeholder="Search conversations..."
        prefix-icon="Search"
        clearable
      />
    </div>

    <div class="session-list">
      <div
        v-for="session in filteredSessions"
        :key="session.id"
        class="session-item"
        :class="{ active: session.id === chatStore.currentSessionId }"
        @click="selectSession(session.id)"
      >
        <div class="session-icon">
          <el-icon><ChatDotRound /></el-icon>
        </div>
        <div class="session-info">
          <div class="session-title">{{ session.title }}</div>
          <div class="session-date">{{ formatDate(session.updatedAt) }}</div>
        </div>
        <div class="session-actions">
          <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, session.id)">
            <span class="el-dropdown-link" @click.stop>
              <el-icon><More /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="rename">Rename</el-dropdown-item>
                <el-dropdown-item command="delete" divided class="text-danger">Delete</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <div class="sidebar-footer">
      <div class="user-profile">
        <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
        <span class="username">User</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useChatStore } from '@/stores/chatStore';
import { Plus, Search, ChatDotRound, More } from '@element-plus/icons-vue';
import { ElMessageBox, ElMessage } from 'element-plus';
import { formatDistanceToNow } from 'date-fns';

const chatStore = useChatStore();
const searchQuery = ref('');

// Load sessions on mount
onMounted(async () => {
  try {
    await chatStore.loadSessions();
  } catch (error) {
    console.error('Failed to load sessions:', error);
  }
});

const filteredSessions = computed(() => {
  if (!searchQuery.value) {
    return chatStore.sortedSessions;
  }
  const query = searchQuery.value.toLowerCase();
  return chatStore.sortedSessions.filter(s => 
    s.title.toLowerCase().includes(query) || 
    s.messages.some(m => m.content.toLowerCase().includes(query))
  );
});

const createNewSession = async () => {
  try {
    await chatStore.createSession();
  } catch (error) {
    ElMessage.error('Failed to create session');
  }
};

const selectSession = async (id: string) => {
  try {
    await chatStore.switchSession(id);
  } catch (error) {
    console.error('Failed to switch session:', error);
    ElMessage.error('Failed to load session');
  }
};

const handleCommand = (command: string, sessionId: string) => {
  if (command === 'delete') {
    ElMessageBox.confirm(
      'Are you sure you want to delete this conversation?',
      'Warning',
      {
        confirmButtonText: 'Delete',
        cancelButtonText: 'Cancel',
        type: 'warning',
      }
    ).then(() => {
      chatStore.deleteSession(sessionId);
      ElMessage.success('Conversation deleted');
    }).catch(() => {});
  } else if (command === 'rename') {
    ElMessageBox.prompt('Please input new title', 'Rename', {
      confirmButtonText: 'OK',
      cancelButtonText: 'Cancel',
    }).then(({ value }) => {
      if (value) {
        chatStore.updateSessionTitle(sessionId, value);
        ElMessage.success('Renamed successfully');
      }
    }).catch(() => {});
  }
};

const formatDate = (timestamp: number) => {
  return formatDistanceToNow(timestamp, { addSuffix: true });
};
</script>

<style scoped>
.sidebar {
  width: 280px;
  height: 100%;
  background-color: var(--color-bg-sidebar);
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: var(--spacing-lg);
}

.new-chat-btn {
  width: 100%;
  justify-content: center;
}

.search-box {
  padding: 0 var(--spacing-lg) var(--spacing-md);
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 var(--spacing-sm);
}

.session-item {
  display: flex;
  align-items: center;
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background-color 0.2s;
  margin-bottom: var(--spacing-xs);
}

.session-item:hover {
  background-color: var(--color-bg-page);
}

.session-item.active {
  background-color: #EBF1FF; /* Light primary */
}

.session-icon {
  margin-right: var(--spacing-md);
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
}

.session-info {
  flex: 1;
  overflow: hidden;
}

.session-title {
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--color-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-date {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
  margin-top: 2px;
}

.session-actions {
  opacity: 0;
  transition: opacity 0.2s;
}

.session-item:hover .session-actions {
  opacity: 1;
}

.el-dropdown-link {
  cursor: pointer;
  color: var(--color-text-secondary);
  padding: 4px;
}

.el-dropdown-link:hover {
  color: var(--color-text-primary);
}

.sidebar-footer {
  padding: var(--spacing-lg);
  border-top: 1px solid var(--color-border);
}

.user-profile {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.username {
  font-size: var(--font-size-sm);
  font-weight: 500;
}

.text-danger {
  color: var(--color-danger);
}
</style>
