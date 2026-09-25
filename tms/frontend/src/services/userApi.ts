import type { User, UserRole, UserSummary } from '../types/api'
import { apiRequest } from './apiClient'

export function listUsers(): Promise<{ content: User[] }> {
  return apiRequest<{ content: User[] }>('/admin/users')
}

export function createUser(payload: {
  username: string
  password: string
  displayName: string
  email: string
  role: UserRole
}): Promise<User> {
  return apiRequest<User>('/admin/users', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function resetPassword(id: number, password: string): Promise<void> {
  return apiRequest<void>(`/admin/users/${id}/reset-password`, {
    method: 'PUT',
    body: JSON.stringify({ password }),
  })
}

export function updateRole(id: number, role: UserRole): Promise<User> {
  return apiRequest<User>(`/admin/users/${id}/role`, {
    method: 'PUT',
    body: JSON.stringify({ role }),
  })
}

export function listAssignees(): Promise<UserSummary[]> {
  return apiRequest<UserSummary[]>('/users/assignees')
}
