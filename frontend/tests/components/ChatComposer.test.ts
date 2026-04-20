import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import ElementPlus from 'element-plus';
import ChatComposer from '@/components/chat/ChatComposer.vue';

describe('ChatComposer', () => {
  it('点击发送按钮会抛出 send 事件', async () => {
    const wrapper = mount(ChatComposer, {
      props: {
        canRetry: true,
        isStreaming: false,
        modelValue: '测试消息',
      },
      global: {
        plugins: [ElementPlus],
      },
    });

    await wrapper.get('.el-button--primary').trigger('click');

    expect(wrapper.emitted('send')).toHaveLength(1);
  });
});
