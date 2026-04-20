import { computed, ref, watch } from 'vue';
import { defineStore } from 'pinia';
import type { ChatMessage, ChatMessageStatus, ChatRole, ChatSession } from '@/types/chat';

const CHAT_STORAGE_KEY = 'xiaozhi-med-chat';

interface PersistedChatState {
  activeSessionId: string;
  sessions: ChatSession[];
}

function buildId(prefix: string) {
  return `${prefix}-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`;
}

function createMemberId() {
  return Number(String(Date.now()).slice(-6));
}

function createSession(title = '新会话', memberId = createMemberId()): ChatSession {
  const now = new Date().toISOString();
  return {
    id: buildId('session'),
    title,
    memberId,
    updatedAt: now,
    messages: [],
  };
}

function createInitialState() {
  const firstSession = createSession();
  return {
    activeSessionId: firstSession.id,
    sessions: [firstSession],
  };
}

function loadPersistedChatState(): PersistedChatState {
  if (typeof window === 'undefined') {
    return createInitialState();
  }

  try {
    const raw = window.localStorage.getItem(CHAT_STORAGE_KEY);
    if (!raw) {
      return createInitialState();
    }

    const parsed = JSON.parse(raw) as PersistedChatState;
    if (!parsed.sessions?.length) {
      return createInitialState();
    }

    return parsed;
  } catch {
    return createInitialState();
  }
}

export const useChatStore = defineStore('chat', () => {
  const initialState = loadPersistedChatState();
  const sessions = ref<ChatSession[]>(initialState.sessions);
  const activeSessionId = ref(initialState.activeSessionId);

  const activeSession = computed(() => {
    return sessions.value.find((session) => session.id === activeSessionId.value) ?? null;
  });

  function ensureSession() {
    if (sessions.value.length > 0) {
      return;
    }

    const session = createSession();
    sessions.value = [session];
    activeSessionId.value = session.id;
  }

  function sortSessions() {
    sessions.value = [...sessions.value].sort((left, right) => {
      return new Date(right.updatedAt).getTime() - new Date(left.updatedAt).getTime();
    });
  }

  function touchSession(sessionId: string) {
    const session = sessions.value.find((item) => item.id === sessionId);
    if (!session) {
      return;
    }

    session.updatedAt = new Date().toISOString();
    sortSessions();
  }

  function createNewSession() {
    const session = createSession();
    sessions.value = [session, ...sessions.value];
    activeSessionId.value = session.id;
    return session;
  }

  function switchSession(sessionId: string) {
    if (sessions.value.some((session) => session.id === sessionId)) {
      activeSessionId.value = sessionId;
    }
  }

  function removeSession(sessionId: string) {
    if (sessions.value.length === 1) {
      const session = createSession();
      sessions.value = [session];
      activeSessionId.value = session.id;
      return;
    }

    sessions.value = sessions.value.filter((session) => session.id !== sessionId);
    if (activeSessionId.value === sessionId) {
      activeSessionId.value = sessions.value[0]?.id ?? '';
    }
    ensureSession();
  }

  function updateSessionMember(sessionId: string, memberId: number) {
    const session = sessions.value.find((item) => item.id === sessionId);
    if (!session) {
      return;
    }

    session.memberId = memberId;
    touchSession(sessionId);
  }

  function renameSessionFromPrompt(sessionId: string, prompt: string) {
    const session = sessions.value.find((item) => item.id === sessionId);
    if (!session) {
      return;
    }

    if (session.title === '新会话' || session.messages.length <= 2) {
      session.title = prompt.trim().slice(0, 18) || '新会话';
    }
  }

  function appendMessage(
    sessionId: string,
    role: ChatRole,
    content: string,
    status: ChatMessageStatus = 'done',
  ) {
    const session = sessions.value.find((item) => item.id === sessionId);
    if (!session) {
      throw new Error('会话不存在');
    }

    const message: ChatMessage = {
      id: buildId('message'),
      role,
      content,
      status,
      createdAt: new Date().toISOString(),
    };

    session.messages.push(message);
    touchSession(sessionId);
    return message.id;
  }

  function patchMessage(sessionId: string, messageId: string, patch: Partial<ChatMessage>) {
    const session = sessions.value.find((item) => item.id === sessionId);
    const message = session?.messages.find((item) => item.id === messageId);
    if (!session || !message) {
      return;
    }

    Object.assign(message, patch);
    touchSession(sessionId);
  }

  watch(
    [sessions, activeSessionId],
    () => {
      if (typeof window === 'undefined') {
        return;
      }

      window.localStorage.setItem(
        CHAT_STORAGE_KEY,
        JSON.stringify({
          activeSessionId: activeSessionId.value,
          sessions: sessions.value,
        }),
      );
    },
    { deep: true },
  );

  ensureSession();

  return {
    activeSessionId,
    sessions,
    activeSession,
    appendMessage,
    createNewSession,
    patchMessage,
    removeSession,
    renameSessionFromPrompt,
    switchSession,
    updateSessionMember,
  };
});
