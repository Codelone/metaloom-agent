import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { createSession as createSessionAPI, getSessions, deleteSessionAPI, getSessionMessages } from '@/services/api';

export interface Message {
    id: string;
    role: 'user' | 'assistant' | 'system';
    content: string;
    timestamp: number;
    status?: 'sending' | 'sent' | 'error';
}

export interface Session {
    id: string;
    title: string;
    messages: Message[];
    createdAt: number;
    updatedAt: number;
}

const CURRENT_USER_ID = 'demo-user'; // Fixed user ID for now

export const useChatStore = defineStore('chat', () => {
    // State
    const sessions = ref<Session[]>([]);
    const currentSessionId = ref<string | null>(null);
    const isSidebarOpen = ref(true);
    const isLoading = ref(false);

    // Getters
    const currentSession = computed(() => {
        return sessions.value.find(s => s.id === currentSessionId.value);
    });

    const sortedSessions = computed(() => {
        return [...sessions.value].sort((a, b) => b.updatedAt - a.updatedAt);
    });

    // Actions
    async function createSession(title: string = 'New Chat') {
        try {
            isLoading.value = true;
            const backendSession = await createSessionAPI(CURRENT_USER_ID, title);

            const newSession: Session = {
                id: backendSession.sessionId,
                title: backendSession.title || title,
                messages: [],
                createdAt: new Date(backendSession.createdAt).getTime(),
                updatedAt: new Date(backendSession.lastAccessAt || backendSession.createdAt).getTime(),
            };

            sessions.value.unshift(newSession);
            currentSessionId.value = newSession.id;
            return newSession;
        } catch (error) {
            console.error('Failed to create session:', error);
            throw error;
        } finally {
            isLoading.value = false;
        }
    }

    async function loadSessions() {
        try {
            isLoading.value = true;
            const backendSessions = await getSessions(CURRENT_USER_ID);

            sessions.value = backendSessions.map((bs: any) => ({
                id: bs.sessionId,
                title: bs.title || 'Untitled Chat',
                messages: [], // Messages will be loaded when session is selected
                createdAt: new Date(bs.createdAt).getTime(),
                updatedAt: new Date(bs.lastAccessAt || bs.createdAt).getTime(),
            }));

            // Set current session to the first one if exists and no current session
            if (sessions.value.length > 0) {
                if (!currentSessionId.value) {
                    currentSessionId.value = sessions.value[0].id;
                }
                // Always load messages for the current session (whether restored or new default)
                if (currentSessionId.value) {
                    await loadSessionMessages(currentSessionId.value);
                }
            }
        } catch (error) {
            console.error('Failed to load sessions:', error);
        } finally {
            isLoading.value = false;
        }
    }

    async function loadSessionMessages(sessionId: string) {
        try {
            const session = sessions.value.find(s => s.id === sessionId);
            if (!session) return;

            const backendMessages = await getSessionMessages(sessionId);

            session.messages = backendMessages.map((bm: any) => ({
                id: bm.messageId,
                role: bm.role.toLowerCase() as 'user' | 'assistant' | 'system',
                content: bm.content,
                timestamp: new Date(bm.timestamp).getTime(),
                status: 'sent' as const,
            }));
        } catch (error) {
            console.error('Failed to load session messages:', error);
        }
    }

    async function deleteSession(id: string) {
        try {
            await deleteSessionAPI(id);

            const index = sessions.value.findIndex(s => s.id === id);
            if (index !== -1) {
                sessions.value.splice(index, 1);
                if (currentSessionId.value === id) {
                    if (sessions.value.length > 0) {
                        currentSessionId.value = sessions.value[0].id;
                        await loadSessionMessages(sessions.value[0].id);
                    } else {
                        currentSessionId.value = null;
                    }
                }
            }
        } catch (error) {
            console.error('Failed to delete session:', error);
            throw error;
        }
    }

    async function switchSession(id: string) {
        currentSessionId.value = id;
        await loadSessionMessages(id);
    }

    function updateSessionTitle(id: string, title: string) {
        const session = sessions.value.find(s => s.id === id);
        if (session) {
            session.title = title;
            session.updatedAt = Date.now();
        }
    }

    function addMessage(sessionId: string, message: Message) {
        const session = sessions.value.find(s => s.id === sessionId);
        if (session) {
            session.messages.push(message);
            session.updatedAt = Date.now();
        }
    }

    function updateMessage(sessionId: string, messageId: string, updates: Partial<Message>) {
        const session = sessions.value.find(s => s.id === sessionId);
        if (session) {
            const message = session.messages.find(m => m.id === messageId);
            if (message) {
                Object.assign(message, updates);
            }
        }
    }

    function clearSessions() {
        sessions.value = [];
        currentSessionId.value = null;
    }

    return {
        sessions,
        currentSessionId,
        currentSession,
        sortedSessions,
        isSidebarOpen,
        isLoading,
        createSession,
        loadSessions,
        loadSessionMessages,
        deleteSession,
        switchSession,
        updateSessionTitle,
        addMessage,
        updateMessage,
        clearSessions
    };
}, {
    persist: {
        paths: ['currentSessionId'], // Only persist current session ID
    },
});
