<script setup lang="ts">
import { computed } from 'vue';
import { ElMessageBox } from 'element-plus';
import ChatComposer from '@/components/chat/ChatComposer.vue';
import ChatMessageList from '@/components/chat/ChatMessageList.vue';
import ChatSessionList from '@/components/chat/ChatSessionList.vue';
import { useChatStore } from '@/stores/chat';
import { useChatStream } from '@/composables/useChatStream';

const chatStore = useChatStore();
const {
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
} = useChatStream();

const promptCards = [
  '我发烧两天，伴随头痛和乏力，应该先看什么科？',
  '最近总是胃胀反酸，吃饭后更明显，这种情况常见吗？',
  '我想描述一下孩子咳嗽的情况，先帮我判断要不要尽快就医。',
];

const totalMessages = computed(() => activeSession.value?.messages.length || 0);
const latestAssistantSummary = computed(() => {
  const latest = [...(activeSession.value?.messages || [])]
    .reverse()
    .find((item) => item.role === 'assistant' && item.content.trim());
  return latest?.content.slice(0, 88) || '还没有生成建议，先从你的症状描述开始。';
});

async function removeSession(sessionId: string) {
  const target = sessions.value.find((item) => item.id === sessionId);
  if (!target) {
    return;
  }

  try {
    await ElMessageBox.confirm(`确定删除会话「${target.title}」吗？`, '删除提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    });

    chatStore.removeSession(sessionId);
  } catch {
    // 用户取消时不需要额外提示。
  }
}

function applyPrompt(prompt: string) {
  draft.value = prompt;
}
</script>

<template>
  <div class="chat-layout">
    <section class="chat-sidebar">
      <ChatSessionList
        :active-session-id="chatStore.activeSessionId"
        :sessions="sessions"
        @create="createSession"
        @remove="removeSession"
        @select="switchSession"
      />
    </section>

    <section class="chat-main">
      <el-card class="hero-card">
        <div class="hero-panel">
          <div class="hero-copy">
            <p class="section-eyebrow">Smart Intake</p>
            <h2 class="section-title">先说症状，再看建议</h2>
            <p class="section-copy">
              你可以直接描述不舒服的部位、持续时间、是否发热疼痛，以及最担心的问题。
            </p>

            <div class="hero-prompts">
              <button
                v-for="prompt in promptCards"
                :key="prompt"
                class="prompt-chip"
                type="button"
                @click="applyPrompt(prompt)"
              >
                {{ prompt }}
              </button>
            </div>
          </div>

          <div class="hero-sidecard">
            <p class="sidecard-label">当前会话</p>
            <h3>{{ activeSession?.title || '新会话' }}</h3>
            <p>{{ latestAssistantSummary }}</p>

            <div class="sidecard-stats">
              <div>
                <span>消息数</span>
                <strong>{{ totalMessages }}</strong>
              </div>
              <div>
                <span>状态</span>
                <strong>{{ isStreaming ? '问询中' : '待输入' }}</strong>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <el-card class="dialog-card">
        <ChatMessageList :messages="activeSession?.messages || []" />
        <ChatComposer
          v-model="draft"
          :can-retry="canRetry"
          :is-streaming="isStreaming"
          @retry="retryLast"
          @send="sendMessage"
          @stop="stopStream"
        />
      </el-card>
    </section>
  </div>
</template>

<style scoped>
.chat-layout {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 20px;
  min-height: calc(100vh - 160px);
}

.chat-sidebar,
.chat-main {
  min-width: 0;
}

.chat-main {
  display: grid;
  gap: 18px;
}

.hero-card {
  overflow: hidden;
  background:
    radial-gradient(circle at top right, rgba(255, 176, 122, 0.24), transparent 24%),
    linear-gradient(135deg, rgba(255, 250, 243, 0.98), rgba(236, 248, 245, 0.96));
}

.hero-card :deep(.el-card__body) {
  padding: 30px;
}

.hero-panel {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(280px, 0.8fr);
  gap: 24px;
  align-items: stretch;
}

.hero-copy {
  display: grid;
  align-content: start;
}

.hero-prompts {
  display: grid;
  gap: 12px;
  margin-top: 22px;
}

.prompt-chip {
  padding: 16px 18px;
  border: 1px solid rgba(17, 70, 70, 0.08);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.72);
  color: var(--ink-soft);
  text-align: left;
  cursor: pointer;
  transition:
    transform 180ms ease,
    border-color 180ms ease,
    box-shadow 180ms ease;
}

.prompt-chip:hover {
  transform: translateY(-2px);
  border-color: rgba(226, 114, 54, 0.24);
  box-shadow: 0 14px 26px rgba(226, 114, 54, 0.08);
}

.hero-sidecard {
  padding: 22px;
  border: 1px solid rgba(17, 70, 70, 0.08);
  border-radius: 24px;
  background: linear-gradient(180deg, rgba(29, 93, 88, 0.96), rgba(22, 54, 54, 0.94));
  color: rgba(247, 252, 250, 0.96);
  box-shadow: 0 20px 42px rgba(18, 58, 56, 0.24);
}

.hero-sidecard h3 {
  margin: 8px 0 10px;
  font: 700 28px/1.1 Georgia, 'Times New Roman', serif;
}

.hero-sidecard p {
  margin: 0;
  color: rgba(239, 245, 243, 0.78);
  line-height: 1.7;
}

.sidecard-label {
  color: rgba(239, 245, 243, 0.62) !important;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.sidecard-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 22px;
}

.sidecard-stats div {
  padding: 14px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.08);
}

.sidecard-stats span {
  display: block;
  margin-bottom: 8px;
  color: rgba(239, 245, 243, 0.68);
  font-size: 12px;
}

.sidecard-stats strong {
  font-size: 24px;
}

.dialog-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 26px;
  background:
    linear-gradient(180deg, rgba(255, 252, 247, 0.95), rgba(250, 247, 241, 0.92)),
    radial-gradient(circle at top right, rgba(255, 183, 133, 0.1), transparent 24%);
}

@media (max-width: 1180px) {
  .chat-layout {
    grid-template-columns: 1fr;
  }

  .hero-panel {
    grid-template-columns: 1fr;
  }
}
</style>
