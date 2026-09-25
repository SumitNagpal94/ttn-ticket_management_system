import type { LoginResponse, User } from '../types/api'
import { apiRequest } from './apiClient'

export function login(username: string, password: string): Promise<LoginResponse> {
  return apiRequest<LoginResponse>('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
}

export function me(): Promise<User> {
  return apiRequest<User>('/auth/me')
}
