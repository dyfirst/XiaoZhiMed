<script setup lang="ts">
import { RouterView, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useChatStore } from '@/stores/chat';
import { computed } from 'vue';

const router = useRouter();
const authStore = useAuthStore();
const chatStore = useChatStore();
const isLoggedIn = computed(() => authStore.hasToken);

function handleLogout() {
  authStore.logout();
  chatStore.clearSessions();
  router.push('/login');
}
</script>

<template>
  <div class="app-shell">
    <div class="ambient ambient-one"></div>
    <div class="ambient ambient-two"></div>
    <div class="ambient ambient-three"></div>

    <header class="shell-header">
      <div class="brand-block">
        <div class="brand-mark">XM</div>
        <div>
          <p class="brand-kicker">AI TRIAGE ASSISTANT</p>
          <h1>小智健康问询</h1>
          <p class="brand-copy">先描述你的症状、持续时间和担心的问题，小智会一步步帮你梳理。</p>
        </div>
      </div>

      <div class="header-actions">
        <div v-if="isLoggedIn" class="user-info">
          <span class="user-name">{{ authStore.userName }}</span>
          <button class="btn-logout" @click="handleLogout">退出</button>
        </div>
        <div class="trust-card">
          <span class="trust-dot"></span>
          <div>
            <strong>线上问询</strong>
            <p>适合先做分诊建议，不替代线下面诊</p>
          </div>
        </div>
      </div>
    </header>

    <main class="shell-main">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.app-shell {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
}

.shell-header {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: center;
  padding: 28px 32px 12px;
}

.brand-block {
  display: flex;
  gap: 18px;
  align-items: center;
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 62px;
  height: 62px;
  border-radius: 20px;
  background: linear-gradient(145deg, #ff8f57, #de6131);
  color: #fffdf6;
  font: 700 22px/1 'Trebuchet MS', 'Microsoft YaHei', sans-serif;
  box-shadow: 0 20px 48px rgba(226, 114, 54, 0.3);
}

.brand-kicker {
  margin: 0 0 6px;
  color: var(--muted);
  font: 700 11px/1.2 'Trebuchet MS', 'Microsoft YaHei', sans-serif;
  letter-spacing: 0.28em;
}

h1 {
  margin: 0;
  color: var(--ink);
  font: 700 clamp(28px, 4vw, 40px) / 1.05 Georgia, 'Times New Roman', serif;
}

.brand-copy {
  margin: 8px 0 0;
  color: var(--muted);
  font-size: 14px;
}

.header-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  align-items: center;
  gap: 14px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  font-size: 14px;
  color: var(--ink);
  font-weight: 500;
}

.btn-logout {
  padding: 6px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background: #fff;
  color: #666;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-logout:hover {
  border-color: #f56c6c;
  color: #f56c6c;
}

.trust-card {
  display: flex;
  gap: 12px;
  align-items: center;
  min-width: 220px;
  padding: 12px 16px;
  border: 1px solid rgba(17, 70, 70, 0.08);
  border-radius: 22px;
  background: rgba(255, 251, 245, 0.72);
  backdrop-filter: blur(16px);
}

.trust-card strong {
  display: block;
  margin-bottom: 2px;
  color: var(--ink);
}

.trust-card p {
  margin: 0;
  color: var(--muted);
  font-size: 12px;
}

.trust-dot {
  width: 12px;
  height: 12px;
  border-radius: 999px;
  background: linear-gradient(180deg, #43ba97, #1f8d78);
  box-shadow: 0 0 0 6px rgba(67, 186, 151, 0.14);
}

.shell-main {
  padding: 8px 32px 32px;
}

.ambient {
  position: fixed;
  border-radius: 999px;
  filter: blur(6px);
  pointer-events: none;
}

.ambient-one {
  top: -72px;
  left: -80px;
  width: 280px;
  height: 280px;
  background: radial-gradient(circle, rgba(255, 162, 92, 0.34), transparent 70%);
}

.ambient-two {
  top: 20%;
  right: -120px;
  width: 340px;
  height: 340px;
  background: radial-gradient(circle, rgba(63, 164, 154, 0.18), transparent 70%);
}

.ambient-three {
  bottom: -120px;
  left: 18%;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(235, 200, 126, 0.22), transparent 70%);
}

@media (max-width: 900px) {
  .shell-header {
    padding: 20px 18px 10px;
    flex-direction: column;
    align-items: stretch;
  }

  .shell-main {
    padding: 8px 18px 24px;
  }

  .header-actions {
    width: 100%;
  }
}
</style>
