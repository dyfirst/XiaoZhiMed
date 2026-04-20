export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

export interface RequestConfig<TBody = unknown> {
  url: string;
  method?: HttpMethod;
  data?: TBody;
  query?: Record<string, string | number | boolean | undefined | null>;
  headers?: HeadersInit;
  timeoutMs?: number;
  retries?: number;
  auth?: boolean;
  signal?: AbortSignal;
  responseType?: 'json' | 'text' | 'response';
}

export interface ApiErrorOptions {
  status?: number;
  code?: string;
  details?: unknown;
  isTimeout?: boolean;
}
