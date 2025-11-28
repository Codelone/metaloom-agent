// Backend Session DTO types
export interface ChatSessionDTO {
    sessionId: string;
    userId: string;
    title?: string;
    description?: string;
    status?: string;
    createdAt: string;
    lastAccessAt?: string;
    messageCount?: number;
    totalTokens?: number;
}

// Local Session types (for frontend state)
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
