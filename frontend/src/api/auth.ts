import { request } from '@/api/request'

interface LoginData {
  token: string
  userId: number
  name: string
  phone: string
}

interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export async function sendCode(phone: string) {
  const res = await request<ApiResult<void>>({
    url: '/auth/send-code',
    method: 'POST',
    data: { phone },
    auth: false,
  })
  if (res.code !== 200) {
    throw new Error(res.message || '发送验证码失败')
  }
}

export async function login(phone: string, code: string): Promise<LoginData> {
  const res = await request<ApiResult<LoginData>>({
    url: '/auth/login',
    method: 'POST',
    data: { phone, code },
    auth: false,
  })
  if (res.code !== 200) {
    throw new Error(res.message || '登录失败')
  }
  return res.data
}
