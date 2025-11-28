import axios from 'axios';

const api = axios.create({
    baseURL: '/api',
    timeout: 60000,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Session Management APIs
export async function createSession(userId: string, title?: string) {
    const response = await api.post('/chatbot/sessions', { userId, title });
    return response.data;
}

export async function getSessions(userId: string) {
    const response = await api.get(`/chatbot/sessions/${userId}`);
    return response.data;
}

export async function deleteSessionAPI(sessionId: string) {
    await api.delete(`/chatbot/sessions/${sessionId}`);
}

export async function getSessionMessages(sessionId: string) {
    const response = await api.get(`/chatbot/sessions/${sessionId}/messages`);
    return response.data;
}

export async function updateSessionTitle(sessionId: string, title: string) {
    // For now, we don't have a direct API, but we can handle this client-side
    // If backend adds this endpoint, we can use it
    console.log('Update session title:', sessionId, title);
}

// SSE Helper
export async function fetchStream(
    url: string,
    body: any,
    onChunk: (chunk: any) => void,
    onDone: () => void,
    onError: (error: any) => void
) {
    console.log('🌐 [API] fetchStream called with:', { url, body });

    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(body),
        });

        console.log('📡 [API] Response received:', response.status, response.statusText);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const reader = response.body?.getReader();
        const decoder = new TextDecoder();

        if (!reader) {
            throw new Error('Response body is null');
        }

        let buffer = '';
        let chunkCount = 0;

        while (true) {
            const { done, value } = await reader.read();
            if (done) {
                console.log('🏁 [API] Stream done, total chunks:', chunkCount);
                // Stream ended without [DONE] marker, call onDone anyway
                onDone();
                break;
            }

            const chunk = decoder.decode(value, { stream: true });
            buffer += chunk;

            console.log('📥 [API] Raw chunk received:', chunk.substring(0, 100));

            const lines = buffer.split('\n');
            // Keep the last line in the buffer as it might be incomplete
            buffer = lines.pop() || '';

            for (const line of lines) {
                const trimmedLine = line.trim();
                if (trimmedLine === '') continue;

                console.log('📄 [API] Processing line:', line);

                // Handle SSE format: lines starting with "data:" or "data: "
                if (trimmedLine.startsWith('data:')) {
                    // Remove "data:" prefix (with or without space)
                    let data = trimmedLine.startsWith('data: ')
                        ? trimmedLine.slice(6)  // "data: " with space
                        : trimmedLine.slice(5);  // "data:" without space

                    // Sometimes data might have duplicate "data:" prefix, clean it
                    while (data.startsWith('data:')) {
                        data = data.startsWith('data: ') ? data.slice(6) : data.slice(5);
                    }

                    data = data.trim();
                    console.log('📦 [API] Data part:', data);

                    if (data === '[DONE]') {
                        console.log('✅ [API] Received [DONE] marker');
                        onDone();
                        return;
                    }

                    if (data === '') {
                        // Empty data line, skip
                        continue;
                    }

                    try {
                        const parsed = JSON.parse(data);
                        console.log('✨ [API] Parsed JSON:', parsed);
                        chunkCount++;
                        onChunk(parsed);
                    } catch (e) {
                        console.error('❌ [API] Error parsing SSE chunk:', e, 'Data:', data);
                    }
                }
            }
        }
    } catch (error) {
        console.error('💥 [API] fetchStream error:', error);
        onError(error);
    }
}

export default api;
