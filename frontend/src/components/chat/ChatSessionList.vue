<script setup lang="ts">
import { computed } from 'vue';
import type { ChatSession } from '@/types/chat';

const props = defineProps<{
  activeSessionId: string;
  sessions: ChatSession[];
}>();

const emits = defineEmits<{
  create: [];
  remove: [sessionId: string];
  select: [sessionId: string];
}>();

const sortedSessions = computed(() => {
  return [...props.sessions].sort((left, right) => {
    return new Date(right.updatedAt).getTime() - new Date(left.updatedAt).getTime();
  });
});

function buildPreview(session: ChatSession) {
  const latest = [...session.messages].reverse().find((item) => item.role === 'assistant' || item.role === 'user');
  return latest?.content || '从这里开始新的问诊。';
}

function buildTime(session: ChatSession) {
  return new Date(session.updatedAt).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
  });
}
</script>

<template>
  <el-card class="session-panel">
    <template #header>
        <div class="panel-header">
          <div>
            <p class="section-eyebrow">Conversation</p>
            <h3>问询记录</h3>
          </div>
          <el-button type="primary" round @click="emits('create')">新建会话</el-button>
        </div>
    </template>

    <el-scrollbar max-height="620px">
      <div class="session-list">
        <article
          v-for="session in sortedSessions"
          :key="session.id"
          class="session-item"
          :class="{ 'is-active': session.id === activeSessionId }"
          role="button"
          tabindex="0"
          @click="emits('select', session.id)"
          @keydown.enter.prevent="emits('select', session.id)"
          @keydown.space.prevent="emits('select', session.id)"
        >
          <div class="session-meta">
            <span class="session-title">{{ session.title }}</span>
            <span class="session-time">{{ buildTime(session) }}</span>
          </div>

          <p class="session-preview">{{ buildPreview(session) }}</p>

          <div class="session-footer">
            <span>{{ session.messages.length }} 条消息</span>
            <el-button link type="danger" @click.stop="emits('remove', session.id)">删除</el-button>
          </div>
        </article>
      </div>
    </el-scrollbar>
  </el-card>
</template>

<style scoped>
.session-panel {
  height: 100%;
  background:
    linear-gradient(180deg, rgba(255, 252, 248, 0.96), rgba(246, 241, 233, 0.9)),
    radial-gradient(circle at top, rgba(255, 184, 136, 0.12), transparent 28%);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

h3 {
  margin: 0;
  font: 700 26px/1.1 Georgia, 'Times New Roman', serif;
}

.session-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.session-item {
  width: 100%;
  padding: 16px;
  border: 1px solid rgba(17, 70, 70, 0.06);
  border-radius: 22px;
  background: rgba(255, 254, 251, 0.92);
  color: inherit;
  text-align: left;
  cursor: pointer;
  transition:
    transform 180ms ease,
    border-color 180ms ease,
    box-shadow 180ms ease;
}

.session-item:hover,
.session-item.is-active {
  transform: translateY(-2px);
  border-color: rgba(47, 143, 135, 0.28);
  box-shadow: 0 16px 30px rgba(47, 143, 135, 0.08);
}

.session-item.is-active {
  background: linear-gradient(145deg, rgba(245, 255, 252, 0.96), rgba(255, 248, 241, 0.94));
}

.session-meta,
.session-footer {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: center;
}

.session-title {
  font-weight: 700;
}

.session-time {
  color: var(--muted);
  font-size: 12px;
}

.session-preview {
  display: -webkit-box;
  margin: 12px 0;
  overflow: hidden;
  color: var(--muted);
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.session-footer {
  color: var(--muted);
  font-size: 12px;
}
</style>
