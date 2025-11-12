export interface StepEvent {
  type?: string;
  request?: string;
  result?: unknown;
}

export interface AnalysisRequest {
  query: string;
}

export async function streamAnalysis(
  baseUrl: string,
  payload: AnalysisRequest,
  onEvent: (event: StepEvent) => void,
  onError?: (error: Error) => void
): Promise<void> {
  const controller = new AbortController();
  try {
    const response = await fetch(`${baseUrl}/api/analysis/stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload),
      signal: controller.signal
    });

    if (!response.ok || !response.body) {
      throw new Error(`SSE 请求失败: ${response.status} ${response.statusText}`);
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder('utf-8');
    let buffer = '';

    while (true) {
      const { value, done } = await reader.read();
      if (done) break;
      buffer += decoder.decode(value, { stream: true });

      // SSE 以 \n\n 分隔事件
      let boundary = buffer.indexOf('\n\n');
      while (boundary !== -1) {
        const rawEvent = buffer.slice(0, boundary);
        buffer = buffer.slice(boundary + 2);
        boundary = buffer.indexOf('\n\n');

        // 解析单个事件块
        const lines = rawEvent.split('\n');
        let eventName = '';
        const dataLines: string[] = [];
        for (const line of lines) {
          if (line.startsWith('event:')) {
            eventName = line.slice(6).trim();
          } else if (line.startsWith('data:')) {
            dataLines.push(line.slice(5).trim());
          }
        }
        const dataStr = dataLines.join('\n');
        try {
          const parsed: StepEvent = JSON.parse(dataStr);
          // 补充 name 到 request 字段，便于 UI 标题显示
          if (eventName && !parsed.request) {
            parsed.request = eventName;
          }
          onEvent(parsed);
        } catch (e) {
          console.warn('事件解析失败', e, dataStr);
        }
      }
    }
  } catch (err) {
    if (onError && err instanceof Error) onError(err);
  } finally {
    controller.abort();
  }
} 