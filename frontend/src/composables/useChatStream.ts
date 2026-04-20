import { computed, ref, shallowRef } from 'vue';
import { ElMessage } from 'element-plus';
import { openChatStream } from '@/api/chat';
import { ApiError } from '@/api/request';
import { useChatStore } from '@/stores/chat';
import type { ChatPayload } from '@/types/chat';

interface LastRequest {
  sessionId: string;
  payload: ChatPayload;
}

export function useChatStream() {
  const chatStore = useChatStore();
  const draft = ref('');
  const isStreaming = ref(false);
  const controller = shallowRef<AbortController | null>(null);
  const lastRequest = shallowRef<LastRequest | null>(null);

  const sessions = computed(() => chatStore.sessions);
  const activeSession = computed(() => chatStore.activeSession);
  const canRetry = computed(() => Boolean(lastRequest.value) && !isStreaming.value);

  function ensureActiveSession() {
    return activeSession.value ?? chatStore.createNewSession();
  }

  async function consumeStream(sessionId: string, assistantMessageId: string, payload: ChatPayload) {
    const currentController = new AbortController();
    controller.value = currentController;
    isStreaming.value = true;

    try {
      const response = await openChatStream(payload, currentController.signal);
      const reader = response.body?.getReader();

      if (!reader) {
        throw new ApiError('后端未返回可读取的流式内容');
      }

      const decoder = new TextDecoder('utf-8');
      let fullText = '';

      while (true) {
        const { done, value } = await reader.read();
        if (done) {
          break;
        }

        fullText += decoder.decode(value, { stream: true });
        chatStore.patchMessage(sessionId, assistantMessageId, {
          content: fullText,
          status: 'streaming',
        });
      }

      fullText += decoder.decode();

      chatStore.patchMessage(sessionId, assistantMessageId, {
        content: fullText.trim() ? fullText : '暂未收到模型内容',
        status: 'done',
        errorMessage: undefined,
      });
    } catch (error) {
      if (currentController.signal.aborted) {
        chatStore.patchMessage(sessionId, assistantMessageId, {
          status: 'interrupted',
          errorMessage: '本次回答已被打断',
        });
        return;
      }

      const normalized = error instanceof ApiError ? error : new ApiError('流式对话失败');
      chatStore.patchMessage(sessionId, assistantMessageId, {
        status: 'error',
        errorMessage: normalized.message,
        content: normalized.message,
      });
      ElMessage.error(normalized.message);
    } finally {
      isStreaming.value = false;
      controller.value = null;
    }
  }

  async function sendMessage(rawText?: string) {
    const content = (rawText ?? draft.value).trim();
    if (!content) {
      return;
    }

    if (isStreaming.value) {
      stopStream();
    }

    const session = ensureActiveSession();
    draft.value = '';
    chatStore.renameSessionFromPrompt(session.id, content);
    chatStore.appendMessage(session.id, 'user', content, 'done');
    const assistantMessageId = chatStore.appendMessage(session.id, 'assistant', '', 'streaming');

    const payload: ChatPayload = {
      memberId: session.memberId,
      message: content,
    };

    lastRequest.value = {
      sessionId: session.id,
      payload,
    };

    await consumeStream(session.id, assistantMessageId, payload);
  }

  async function retryLast() {
    if (!lastRequest.value || isStreaming.value) {
      return;
    }

    const assistantMessageId = chatStore.appendMessage(
      lastRequest.value.sessionId,
      'assistant',
      '',
      'streaming',
    );

    await consumeStream(lastRequest.value.sessionId, assistantMessageId, lastRequest.value.payload);
  }

  function stopStream() {
    controller.value?.abort('用户主动打断');
  }

  function switchSession(sessionId: string) {
    if (isStreaming.value) {
      stopStream();
    }
    chatStore.switchSession(sessionId);
  }

  function createSession() {
    if (isStreaming.value) {
      stopStream();
    }
    return chatStore.createNewSession();
  }

  return {
    activeSession,
    canRetry,
    createSession,
    draft,
    isStreaming,
    retryLast,
    sendMessage,
    sessions,
    stopStream,
    switchSession,
  };
}
