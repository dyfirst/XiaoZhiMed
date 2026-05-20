<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const chatStore = useChatStore()

const phone = ref('')
const code = ref('')
const loading = ref(false)
const codeSent = ref(false)
const countdown = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

async function handleSendCode() {
  if (!phone.value || phone.value.length !== 11) {
    ElMessage.warning('请输入正确的11位手机号')
    return
  }
  try {
    await authStore.requestSendCode(phone.value)
    codeSent.value = true
    countdown.value = 60
    ElMessage.success('验证码已发送（模拟验证码：123456）')
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer!)
        timer = null
      }
    }, 1000)
  } catch {
    ElMessage.error('发送验证码失败')
  }
}

async function handleLogin() {
  if (!phone.value || phone.value.length !== 11) {
    ElMessage.warning('请输入正确的11位手机号')
    return
  }
  if (!code.value) {
    ElMessage.warning('请输入验证码')
    return
  }
  loading.value = true
  try {
    await authStore.login(phone.value, code.value)
    await chatStore.loadFromRemote()
    ElMessage.success('登录成功')
    router.push('/chat')
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '登录失败'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2>华小智</h2>
        <p>华西医院智能导诊助手</p>
      </div>
      <div class="login-form">
        <div class="form-item">
          <input
            v-model="phone"
            type="tel"
            maxlength="11"
            placeholder="请输入手机号"
            class="input-field"
          />
        </div>
        <div class="form-item code-row">
          <input
            v-model="code"
            type="text"
            maxlength="6"
            placeholder="请输入验证码"
            class="input-field code-input"
            @keyup.enter="handleLogin"
          />
          <button
            class="btn-code"
            :disabled="countdown > 0"
            @click="handleSendCode"
          >
            {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
          </button>
        </div>
        <button class="btn-login" :disabled="loading" @click="handleLogin">
          {{ loading ? '登录中...' : '登录' }}
        </button>
        <p class="hint">模拟验证码：123456</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px 36px;
  width: 380px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-header h2 {
  font-size: 28px;
  color: #333;
  margin: 0 0 8px;
}

.login-header p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.form-item {
  margin-bottom: 16px;
}

.code-row {
  display: flex;
  gap: 12px;
}

.input-field {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  font-size: 15px;
  outline: none;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.input-field:focus {
  border-color: #667eea;
}

.code-input {
  flex: 1;
}

.btn-code {
  white-space: nowrap;
  padding: 12px 16px;
  border: 1px solid #667eea;
  background: #fff;
  color: #667eea;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-code:hover:not(:disabled) {
  background: #667eea;
  color: #fff;
}

.btn-code:disabled {
  border-color: #ccc;
  color: #ccc;
  cursor: not-allowed;
}

.btn-login {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  margin-top: 8px;
  transition: opacity 0.2s;
}

.btn-login:hover:not(:disabled) {
  opacity: 0.9;
}

.btn-login:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.hint {
  text-align: center;
  font-size: 12px;
  color: #bbb;
  margin: 16px 0 0;
}
</style>
