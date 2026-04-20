import type { ApiErrorOptions, RequestConfig } from '@/types/api';
import { readPersistedToken } from '@/stores/auth';

const DEFAULT_TIMEOUT_MS = 15000;

export class ApiError extends Error {
  status?: number;
  code?: string;
  details?: unknown;
  isTimeout?: boolean;

  constructor(message: string, options: ApiErrorOptions = {}) {
    super(message);
    this.name = 'ApiError';
    this.status = options.status;
    this.code = options.code;
    this.details = options.details;
    this.isTimeout = options.isTimeout;
  }
}

function delay(ms: number) {
  return new Promise((resolve) => window.setTimeout(resolve, ms));
}

function buildUrl(config: RequestConfig) {
  const baseUrl = (import.meta.env.VITE_API_BASE_URL || '/api').replace(/\/$/, '');
  const rawUrl = config.url.startsWith('http') ? config.url : `${baseUrl}${config.url}`;
  const url = new URL(rawUrl, window.location.origin);

  Object.entries(config.query ?? {}).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      return;
    }
    url.searchParams.set(key, String(value));
  });

  if (url.origin === window.location.origin) {
    return `${url.pathname}${url.search}`;
  }

  return url.toString();
}

function createHeaders(config: RequestConfig, hasBody: boolean) {
  const headers = new Headers(config.headers);

  if (hasBody && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  if (!headers.has('Accept')) {
    headers.set('Accept', 'application/json, text/plain;q=0.9, */*;q=0.8');
  }

  if (config.auth !== false) {
    const token = readPersistedToken();
    if (token) {
      headers.set('Authorization', `Bearer ${token}`);
    }
  }

  return headers;
}

function connectAbortSignals(externalSignal: AbortSignal | undefined, timeoutMs: number) {
  const controller = new AbortController();
  const timeoutId = window.setTimeout(() => {
    controller.abort(new ApiError('请求超时，请稍后重试', { isTimeout: true }));
  }, timeoutMs);

  if (externalSignal) {
    if (externalSignal.aborted) {
      controller.abort(externalSignal.reason);
    } else {
      externalSignal.addEventListener(
        'abort',
        () => controller.abort(externalSignal.reason),
        { once: true },
      );
    }
  }

  return {
    controller,
    cleanup() {
      window.clearTimeout(timeoutId);
    },
  };
}

async function parseError(response: Response) {
  const text = await response.text();
  const message = text || `请求失败，状态码 ${response.status}`;
  return new ApiError(message, {
    status: response.status,
    details: text,
  });
}

function normalizeError(error: unknown) {
  if (error instanceof ApiError) {
    return error;
  }

  if (error instanceof DOMException && error.name === 'AbortError') {
    return new ApiError('请求已取消', {
      code: 'ABORTED',
    });
  }

  if (error instanceof Error) {
    return new ApiError(error.message || '网络请求失败', {
      details: error,
    });
  }

  return new ApiError('网络请求失败');
}

function shouldRetry(error: ApiError) {
  if (error.code === 'ABORTED') {
    return false;
  }

  return Boolean(error.isTimeout || !error.status || error.status >= 500);
}

async function executeRequest(config: RequestConfig) {
  const method = config.method ?? 'GET';
  const attempts = Math.max(0, config.retries ?? (method === 'GET' ? 1 : 0)) + 1;
  const body = config.data === undefined ? undefined : JSON.stringify(config.data);

  let lastError: ApiError | null = null;

  for (let attempt = 0; attempt < attempts; attempt += 1) {
    const { controller, cleanup } = connectAbortSignals(config.signal, config.timeoutMs ?? DEFAULT_TIMEOUT_MS);

    try {
      const response = await fetch(buildUrl(config), {
        method,
        body,
        headers: createHeaders(config, body !== undefined),
        signal: controller.signal,
      });

      if (!response.ok) {
        throw await parseError(response);
      }

      return response;
    } catch (error) {
      const normalized = normalizeError(error);
      lastError = normalized;

      const canRetry = attempt < attempts - 1 && shouldRetry(normalized);
      if (!canRetry) {
        throw normalized;
      }

      await delay(300 * (attempt + 1));
    } finally {
      cleanup();
    }
  }

  throw lastError ?? new ApiError('请求失败');
}

export async function requestRaw(config: RequestConfig) {
  return executeRequest(config);
}

export async function request<T>(config: RequestConfig): Promise<T> {
  const response = await executeRequest(config);

  if (config.responseType === 'response') {
    return response as T;
  }

  if (config.responseType === 'text') {
    return (await response.text()) as T;
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}
