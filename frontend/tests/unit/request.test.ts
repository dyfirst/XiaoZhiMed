import { beforeEach, describe, expect, it, vi } from 'vitest';
import { request } from '@/api/request';

describe('request', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('会在服务端错误后按配置重试一次', async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        new Response('server error', {
          status: 500,
        }),
      )
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ ok: true }), {
          status: 200,
          headers: {
            'Content-Type': 'application/json',
          },
        }),
      );

    vi.stubGlobal('fetch', fetchMock);

    const result = await request<{ ok: boolean }>({
      url: '/health',
      method: 'GET',
      retries: 1,
    });

    expect(result.ok).toBe(true);
    expect(fetchMock).toHaveBeenCalledTimes(2);
  });
});
