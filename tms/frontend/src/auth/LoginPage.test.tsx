import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { AuthContext } from './AuthContext'
import { LoginPage } from './LoginPage'

describe('LoginPage', () => {
  it('renders sign in form', () => {
    render(
      <MemoryRouter>
        <AuthContext.Provider value={{
          user: null,
          token: null,
          role: null,
          loading: false,
          login: vi.fn(),
          logout: vi.fn(),
          refresh: vi.fn(),
        }}>
          <LoginPage />
        </AuthContext.Provider>
      </MemoryRouter>,
    )
    expect(screen.getByRole('heading', { name: 'Sign in' })).toBeTruthy()
    expect(screen.getByRole('button', { name: 'Sign in' })).toBeTruthy()
  })
})
