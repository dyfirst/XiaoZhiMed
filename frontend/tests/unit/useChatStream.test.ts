import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { nextTick } from 'vue';
import { useChatStream } from '@/composables/useChatStream';
import { useChatStore } from '@/stores/chat';

function createStreamResponse(chunks: string[]) {
  const encoder = new TextEncoder();

  return new Response(
    new ReadableStream({
      start(controller) {
        chunks.forEach((chunk) => controller.enqueue(encoder.encode(chunk)));
        controller.close();
      },
    }),
    {
      status: 200,
      headers: {
        'Content-Type': 'text/plain; charset=utf-8',
      },
    },
  );
}

describe('useChatStream', () => {
  beforeEach(() => {
    window.localStorage.clear();
    setActivePinia(createPinia());
    vi.restoreAllMocks();
  });

  it('会把流式分片拼接到当前会话', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(createStreamResponse(['你好，', '这里是小智。'])));

    const composable = useChatStream();
    composable.draft.value = '我头疼怎么办';

    await composable.sendMessage();
    await nextTick();

    const store = useChatStore();
    const messages = store.activeSession?.messages ?? [];
    expect(messages).toHaveLength(2);
    expect(messages[1]?.content).toContain('这里是小智');
    expect(messages[1]?.status).toBe('done');
  });
});
