<script setup lang="ts">
import { computed } from 'vue';
import type { ChatMessage } from '@/types/chat';

const props = defineProps<{
  messages: ChatMessage[];
}>();

const empty = computed(() => props.messages.length === 0);

function statusLabel(message: ChatMessage) {
  switch (message.status) {
    case 'streaming':
      return '生成中';
    case 'error':
      return '失败';
    case 'interrupted':
      return '已打断';
    default:
      return message.role === 'assistant' ? '已完成' : '已发送';
  }
}

function statusType(message: ChatMessage) {
  switch (message.status) {
    case 'error':
      return 'danger';
    case 'interrupted':
      return 'warning';
    case 'streaming':
      return 'success';
    default:
      return 'info';
  }
}
</script>

<template>
  <div class="message-list">
    <el-empty
      v-if="empty"
      description="从一条症状描述开始，系统会通过后端流式返回回复。"
    />

    <template v-else>
      <article
        v-for="message in messages"
        :key="message.id"
        class="message-item"
        :class="`role-${message.role}`"
      >
        <div class="message-head">
          <div class="message-author">
            <span class="message-avatar">
              {{ message.role === 'assistant' ? 'AI' : 'YOU' }}
            </span>
            <div>
              <strong>{{ message.role === 'assistant' ? '小智医生助理' : '用户提问' }}</strong>
              <p>{{ new Date(message.createdAt).toLocaleString('zh-CN') }}</p>
            </div>
          </div>
          <el-tag :type="statusType(message)" effect="plain">
            {{ statusLabel(message) }}
          </el-tag>
        </div>

        <pre class="message-body">{{ message.content || '...' }}</pre>

        <p v-if="message.errorMessage && message.status !== 'done'" class="message-error">
          {{ message.errorMessage }}
        </p>
      </article>
    </template>
  </div>
</template>

<style scoped>
.message-list {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 18px;
  min-height: 420px;
  max-height: 64vh;
  padding-right: 6px;
  overflow: auto;
}

.message-item {
  padding: 20px;
  border: 1px solid rgba(17, 70, 70, 0.06);
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 14px 28px rgba(73, 53, 36, 0.05);
}

.role-user {
  align-self: flex-end;
  width: min(100%, 760px);
  background: linear-gradient(145deg, rgba(255, 244, 235, 0.98), rgba(255, 251, 245, 0.94));
}

.role-assistant {
  width: min(100%, 840px);
  background: linear-gradient(145deg, rgba(241, 252, 250, 0.98), rgba(255, 250, 243, 0.92));
}

.message-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.message-author {
  display: flex;
  gap: 12px;
}

.message-author strong {
  display: block;
  margin-bottom: 4px;
}

.message-author p {
  margin: 0;
  color: var(--muted);
  font-size: 12px;
}

.message-avatar {
  display: grid;
  place-items: center;
  width: 46px;
  height: 46px;
  border-radius: 16px;
  background: var(--secondary-soft);
  color: var(--secondary);
  font-weight: 700;
}

.role-user .message-avatar {
  background: var(--accent-soft);
  color: var(--accent);
}

.message-body {
  margin: 16px 0 0;
  white-space: pre-wrap;
  word-break: break-word;
  font: 500 15px/1.75 'Trebuchet MS', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  color: var(--ink-soft);
}

.message-error {
  margin: 12px 0 0;
  color: #b94f3c;
  font-size: 13px;
}
</style>
