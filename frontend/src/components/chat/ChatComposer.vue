<script setup lang="ts">
const props = defineProps<{
  canRetry: boolean;
  isStreaming: boolean;
  modelValue: string;
}>();

const emits = defineEmits<{
  retry: [];
  send: [];
  stop: [];
  'update:modelValue': [value: string];
}>();

function updateValue(value: string) {
  emits('update:modelValue', value);
}
</script>

<template>
  <div class="composer-shell">
    <el-input
      :model-value="props.modelValue"
      type="textarea"
      :autosize="{ minRows: 3, maxRows: 6 }"
      placeholder="输入症状、科室诉求或预约需求，回车发送，Shift + 回车换行"
      @update:model-value="updateValue"
      @keydown.enter.exact.prevent="emits('send')"
    />

    <div class="composer-actions">
      <el-button plain :disabled="!props.canRetry" @click="emits('retry')">重试上次回答</el-button>
      <el-button
        v-if="props.isStreaming"
        type="warning"
        plain
        @click="emits('stop')"
      >
        打断输出
      </el-button>
      <el-button
        v-else
        type="primary"
        :disabled="!props.modelValue.trim()"
        @click="emits('send')"
      >
        发送消息
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.composer-shell {
  display: grid;
  gap: 16px;
  padding-top: 18px;
  border-top: 1px solid rgba(17, 70, 70, 0.08);
}

.composer-shell :deep(.el-textarea__inner) {
  border-radius: 22px;
  padding: 16px 18px;
  background: rgba(255, 255, 255, 0.84);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.composer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  .composer-actions {
    flex-direction: column-reverse;
  }

  .composer-actions .el-button {
    width: 100%;
  }
}
</style>
