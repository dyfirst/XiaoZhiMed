export type ChatRole = 'user' | 'assistant';
export type ChatMessageStatus = 'done' | 'streaming' | 'error' | 'interrupted';

export interface ChatMessage {
  id: string;
  role: ChatRole;
  content: string;
  status: ChatMessageStatus;
  createdAt: string;
  errorMessage?: string;
}

export interface ChatSession {
  id: string;
  title: string;
  memberId: number;
  updatedAt: string;
  messages: ChatMessage[];
}

export interface ChatPayload {
  memberId: number;
  message: string;
}
