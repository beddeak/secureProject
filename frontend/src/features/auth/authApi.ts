import { apiRequest } from '../../api/http'
import type {
  AuthenticatedUserResponse,
  AuthUser,
  LoginRequest,
  LoginResponse,
  SignupRequest,
} from './types'

type SignupResponse = {
  id: number
  email: string
  nickname: string
  role: string
  clearanceLevel: number
  status: string
}

export const authApi = {
  login(request: LoginRequest) {
    return apiRequest<LoginResponse>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(request),
    })
  },

  signup(request: SignupRequest) {
    return apiRequest<SignupResponse>('/api/auth/signup', {
      method: 'POST',
      body: JSON.stringify(request),
    })
  },

  me(accessToken: string) {
    return apiRequest<AuthenticatedUserResponse>(
      '/api/auth/me',
      { method: 'GET' },
      accessToken,
    )
  },
}

export function normalizeLoginUser(response: LoginResponse): AuthUser {
  const payload = response.user

  return {
    id: payload.id,
    email: payload.email,
    nickname: payload.nickname,
    role: payload.role,
    clearanceLevel: payload.clearanceLevel ?? payload.ClearanceLevel ?? 0,
    clearanceLevelName:
      payload.clearanceLevelName ?? payload.ClearanceLevelName,
    title: payload.title,
    status: payload.status,
  }
}
