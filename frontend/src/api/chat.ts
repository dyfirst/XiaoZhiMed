import { requestRaw } from '@/api/request';
import type { ChatPayload } from '@/types/chat';

export function openChatStream(payload: ChatPayload, signal?: AbortSignal) {
  return requestRaw({
    url: '/xiaozhi/chat',
    method: 'POST',
    data: payload,
    signal,
    retries: 0,
    timeoutMs: 60000,
    headers: {
      Accept: 'text/stream, text/plain;q=0.9, */*;q=0.8',
    },
    responseType: 'response',
  });
}
