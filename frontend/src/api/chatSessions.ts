import { request } from '@/api/request'
import type { ChatSession, ChatMessage } from '@/types/chat'

interface ApiResult<T> {
  code: number
  message: string
  data: T
}

interface ChatSessionDoc {
  sessionId: string
  title: string
  messages: string
  updatedAt: string
}

interface LangChain4jMessage {
  text?: string
  type: string
  contents?: Array<{ text: string; type: string }>
}

function parseLangChain4jMessages(json: string): ChatMessage[] {
  if (!json) return []
  try {
    const messages: LangChain4jMessage[] = JSON.parse(json)
    const result: ChatMessage[] = []

    for (const msg of messages) {
      if (msg.type === 'USER' && msg.contents?.[0]?.text) {
        // 提取用户消息，去掉末尾的 RAG 上下文
        let text = msg.contents[0].text
        const ragIndex = text.indexOf('\n\nAnswer using the following information:')
        if (ragIndex > 0) {
          text = text.substring(0, ragIndex)
        }
        result.push({
          id: `user-${result.length}`,
          role: 'user',
          content: text.trim(),
          status: 'done',
          createdAt: new Date().toISOString(),
        })
      } else if (msg.type === 'AI' && msg.text) {
        result.push({
          id: `assistant-${result.length}`,
          role: 'assistant',
          content: msg.text,
          status: 'done',
          createdAt: new Date().toISOString(),
        })
      }
      // 跳过 SYSTEM 和 TOOL_EXECUTION_RESULT
    }

    return result
  } catch {
    return []
  }
}

export async function fetchSessions(): Promise<ChatSession[]> {
  const res = await request<ApiResult<ChatSessionDoc[]>>({
    url: '/chat-sessions',
    method: 'GET',
  })
  if (res.code !== 200 || !res.data) {
    return []
  }
  return res.data.map((doc) => ({
    id: doc.sessionId,
    title: doc.title || '新会话',
    updatedAt: doc.updatedAt,
    messages: parseLangChain4jMessages(doc.messages),
  }))
}

export async function updateSessionTitle(sessionId: string, title: string) {
  await request<ApiResult<void>>({
    url: `/chat-sessions/${sessionId}/title`,
    method: 'PUT',
    data: { title },
  })
}

export async function deleteRemoteSession(sessionId: string) {
  await request<ApiResult<void>>({
    url: `/chat-sessions/${sessionId}`,
    method: 'DELETE',
  })
}
