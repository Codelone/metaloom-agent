<template>
  <div class="chat-panel">
    <div v-if="!currentSession" class="empty-state">
      <div class="empty-content">
        <h1>Welcome to Metaloom</h1>
        <p>Start a new conversation to begin.</p>
        <el-button type="primary" @click="createNewSession">Start Chatting</el-button>
      </div>
    </div>

    <template v-else>
      <div class="chat-header">
        <div class="header-info">
          <h2>{{ currentSession.title }}</h2>
          <span class="message-count">{{ currentSession.messages.length }} messages</span>
        </div>
        <div class="header-actions">
          <el-button circle @click="exportChat">
            <el-icon><Download /></el-icon>
          </el-button>
        </div>
      </div>

      <div class="messages-container" ref="messagesContainer">
        <div v-for="msg in currentSession.messages" :key="msg.id" class="message-wrapper" :class="msg.role">
          <div class="message-avatar">
            <el-avatar :size="36" :icon="msg.role === 'user' ? UserFilled : Service" :class="msg.role" />
          </div>
          <div class="message-content">
            <div class="message-meta">
              <span class="sender">{{ msg.role === 'user' ? 'You' : 'AI Assistant' }}</span>
              <span class="time">{{ formatTime(msg.timestamp) }}</span>
            </div>
            <div class="markdown-body" v-html="renderContent(msg.content)"></div>
            <div v-if="msg.status === 'error'" class="error-status">
              <el-icon><Warning /></el-icon> Failed to send
            </div>
          </div>
        </div>
        <div v-if="isTyping" class="message-wrapper assistant typing">
          <div class="message-avatar">
            <el-avatar :size="36" :icon="Service" class="assistant" />
          </div>
          <div class="message-content">
             <div class="typing-indicator">
               <span></span><span></span><span></span>
             </div>
          </div>
        </div>
      </div>

      <div class="composer-area">
        <div class="composer-wrapper">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="1"
            :autosize="{ minRows: 1, maxRows: 6 }"
            placeholder="Type a message... (Shift+Enter for new line)"
            @keydown.enter.exact.prevent="sendMessage"
            resize="none"
            class="chat-input"
          />
          <el-button type="primary" :loading="isTyping" @click="sendMessage" class="send-btn">
            <el-icon><Position /></el-icon>
          </el-button>
        </div>
        <div class="composer-footer">
          <span class="hint">Enter to send, Shift+Enter for new line</span>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, watch } from 'vue';
import { useChatStore } from '@/stores/chatStore';
import { UserFilled, Service, Download, Position, Warning } from '@element-plus/icons-vue';
import { renderMarkdown } from '@/utils/markdown';
import { fetchStream } from '@/services/api';
import { v4 as uuidv4 } from 'uuid';
import { ElMessage } from 'element-plus';

const chatStore = useChatStore();
const currentSession = computed(() => chatStore.currentSession);
const messagesContainer = ref<HTMLElement | null>(null);
const inputMessage = ref('');
const isTyping = ref(false);

const scrollToBottom = async () => {
  await nextTick();
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
  }
};

watch(() => currentSession.value?.messages.length, scrollToBottom);
watch(() => currentSession.value?.id, scrollToBottom);

const createNewSession = async () => {
  try {
    await chatStore.createSession();
  } catch (error) {
    ElMessage.error('Failed to create new session');
  }
};

const formatTime = (timestamp: number) => {
  return new Date(timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
};

const renderContent = (content: string) => {
  return renderMarkdown(content);
};

const sendMessage = async () => {
  if (!inputMessage.value.trim() || isTyping.value) return;

  const content = inputMessage.value.trim();
  inputMessage.value = '';
  
  try {
    // Create session if needed
    if (!chatStore.currentSessionId) {
      await chatStore.createSession(content.slice(0, 30) + (content.length > 30 ? '...' : ''));
    }

    if (!chatStore.currentSessionId) {
      ElMessage.error('Failed to create session');
      return;
    }

    const sessionId = chatStore.currentSessionId!;
    
    // Add user message
    chatStore.addMessage(sessionId, {
      id: uuidv4(),
      role: 'user',
      content,
      timestamp: Date.now(),
      status: 'sent'
    });

    await scrollToBottom();

    // Prepare assistant message placeholder
    const assistantMsgId = uuidv4();
    isTyping.value = true;
    let fullResponse = '';
    let messageAdded = false;

    console.log('🚀 [ChatPanel] Starting stream request...');

    try {
      await fetchStream(
        '/api/chat/stream',
        { conversationId: sessionId, message: content },
        (chunk) => {
          console.log('📦 [ChatPanel] Received chunk:', chunk);
          
          if (chunk.type === 'message' && chunk.content) {
            fullResponse += chunk.content;
            // console.log('✏️ [ChatPanel] Updated fullResponse:', fullResponse.substring(0, 50) + '...');
            
            if (!messageAdded) {
              // Add assistant message on first chunk
              console.log('➕ [ChatPanel] Adding new assistant message');
              chatStore.addMessage(sessionId, {
                id: assistantMsgId,
                role: 'assistant',
                content: fullResponse,
                timestamp: Date.now(),
                status: 'sending'
              });
              messageAdded = true;
            } else {
              // Update existing message
              console.log('🔄 [ChatPanel] Updating assistant message');
              chatStore.updateMessage(sessionId, assistantMsgId, { content: fullResponse });
            }
            scrollToBottom();
          }
        },
        () => {
          // Stream completed successfully
          console.log('✅ [ChatPanel] Stream completed');
          isTyping.value = false;
          if (messageAdded) {
            chatStore.updateMessage(sessionId, assistantMsgId, { status: 'sent' });
          }
        },
        (error) => {
          // Stream error
          console.error('❌ [ChatPanel] Stream error:', error);
          isTyping.value = false;
          
          if (!messageAdded || !fullResponse) {
            chatStore.addMessage(sessionId, {
              id: assistantMsgId,
              role: 'assistant',
              content: 'Sorry, something went wrong.',
              timestamp: Date.now(),
              status: 'error'
            });
          } else {
            chatStore.updateMessage(sessionId, assistantMsgId, { status: 'error' });
          }
        }
      );
    } catch (streamError) {
      console.error('💥 [ChatPanel] Stream exception:', streamError);
      isTyping.value = false;
      
      if (!messageAdded) {
        chatStore.addMessage(sessionId, {
          id: assistantMsgId,
          role: 'assistant',
          content: 'Sorry, connection failed.',
          timestamp: Date.now(),
          status: 'error'
        });
      }
    }
  } catch (e) {
    console.error('🔥 [ChatPanel] Send message error:', e);
    isTyping.value = false;
    ElMessage.error('Failed to send message');
  }
};

const exportChat = () => {
  if (!currentSession.value) return;
  const data = JSON.stringify(currentSession.value, null, 2);
  const blob = new Blob([data], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `chat-export-${currentSession.value.id}.json`;
  a.click();
  URL.revokeObjectURL(url);
  ElMessage.success('Chat exported');
};
</script>

<style scoped>
.chat-panel {
  flex: 1;
  height: 100%;
  background-color: var(--color-bg-page);
  display: flex;
  flex-direction: column;
  position: relative;
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: var(--color-text-secondary);
}

.empty-content h1 {
  font-size: 2rem;
  margin-bottom: var(--spacing-md);
  color: var(--color-text-primary);
}

.empty-content p {
  margin-bottom: var(--spacing-xl);
}

.chat-header {
  padding: var(--spacing-md) var(--spacing-xl);
  background-color: var(--color-bg-card);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-info h2 {
  font-size: var(--font-size-base);
  margin: 0;
}

.message-count {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-xl);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xl);
}

.message-wrapper {
  display: flex;
  gap: var(--spacing-lg);
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
}

.message-wrapper.user {
  flex-direction: row-reverse;
}

.message-avatar .el-avatar {
  background-color: var(--color-primary);
}

.message-avatar .el-avatar.assistant {
  background-color: var(--color-accent);
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-wrapper.user .message-content {
  text-align: right;
}

.message-meta {
  margin-bottom: var(--spacing-xs);
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
}

.message-wrapper.user .message-meta {
  text-align: right;
}

.message-meta .sender {
  font-weight: 600;
  margin-right: var(--spacing-sm);
}

.message-wrapper.user .message-meta .sender {
  margin-right: 0;
  margin-left: var(--spacing-sm);
}

.markdown-body {
  background-color: var(--color-bg-card);
  padding: var(--spacing-md);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  text-align: left;
  display: inline-block;
}

.message-wrapper.user .markdown-body {
  background-color: var(--color-primary);
  color: white;
}

/* Override markdown styles for user message */
.message-wrapper.user .markdown-body :deep(p) {
  margin-bottom: 0;
}

.composer-area {
  padding: var(--spacing-xl);
  background-color: var(--color-bg-page);
  border-top: 1px solid var(--color-border);
}

.composer-wrapper {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  gap: var(--spacing-md);
  background-color: var(--color-bg-card);
  padding: var(--spacing-sm);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--color-border);
}

.chat-input :deep(.el-textarea__inner) {
  box-shadow: none;
  border: none;
  background: transparent;
  padding: var(--spacing-sm);
}

.composer-footer {
  max-width: 800px;
  margin: var(--spacing-xs) auto 0;
  text-align: center;
}

.hint {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
}

.typing-indicator span {
  display: inline-block;
  width: 6px;
  height: 6px;
  background-color: var(--color-text-secondary);
  border-radius: 50%;
  margin: 0 2px;
  animation: bounce 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}
</style>
