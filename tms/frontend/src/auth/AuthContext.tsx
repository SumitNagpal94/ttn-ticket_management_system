import { createContext, useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import type { User, UserRole } from '../types/api'
import * as authApi from '../services/authApi'

interface AuthState {
  user: User | null
  token: string | null
  role: UserRole | null
  loading: boolean
  login: (username: string, password: string) => Promise<void>
  logout: () => void
  refresh: () => Promise<void>
}

export const AuthContext = createContext<AuthState | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [token, setToken] = useState<string | null>(localStorage.getItem('tms_token'))
  const [loading, setLoading] = useState(true)

  const refresh = useCallback(async () => {
    if (!localStorage.getItem('tms_token')) {
      setUser(null)
      setToken(null)
      setLoading(false)
      return
    }
    try {
      const me = await authApi.me()
      setUser(me)
      setToken(localStorage.getItem('tms_token'))
    } catch {
      localStorage.removeItem('tms_token')
      setUser(null)
      setToken(null)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    refresh()
  }, [refresh])

  const login = useCallback(async (username: string, password: string) => {
    const response = await authApi.login(username, password)
    localStorage.setItem('tms_token', response.token)
    setToken(response.token)
    const me = await authApi.me()
    setUser(me)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('tms_token')
    setUser(null)
    setToken(null)
  }, [])

  const value = useMemo(
    () => ({
      user,
      token,
      role: user?.role ?? null,
      loading,
      login,
      logout,
      refresh,
    }),
    [user, token, loading, login, logout, refresh],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
