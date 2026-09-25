import type { ErrorResponse } from '../types/api'

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '/api'

export class ApiError extends Error {
  code: string
  fieldErrors: { field: string; message: string }[]

  constructor(body: ErrorResponse) {
    super(body.message)
    this.code = body.code
    this.fieldErrors = body.fieldErrors ?? []
    this.name = 'ApiError'
  }
}

function getToken(): string | null {
  return localStorage.getItem('tms_token')
}

export async function apiRequest<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string> | undefined),
  }
  const token = getToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  })

  if (response.status === 204) {
    return undefined as T
  }

  const text = await response.text()
  const body = text ? JSON.parse(text) : null

  if (!response.ok) {
    throw new ApiError(body as ErrorResponse)
  }

  return body as T
}
