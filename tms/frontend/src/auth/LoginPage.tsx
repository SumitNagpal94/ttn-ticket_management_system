import { type FormEvent, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ErrorAlert } from '../components/ErrorAlert'
import { ApiError } from '../services/apiClient'
import { friendlyError } from '../utils/errorMessages'
import { useAuth } from './useAuth'

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<{ field: string; message: string }[]>([])
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setFieldErrors([])
    setSubmitting(true)
    try {
      await login(username, password)
      navigate('/')
    } catch (err) {
      if (err instanceof ApiError) {
        setError(friendlyError(err.code, err.message))
        setFieldErrors(err.fieldErrors)
      } else {
        setError('Login failed')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="page login-page">
      <h1>Sign in</h1>
      <form onSubmit={handleSubmit}>
        {error && <ErrorAlert message={error} fieldErrors={fieldErrors} />}
        <label>
          Username
          <input value={username} onChange={(e) => setUsername(e.target.value)} required />
        </label>
        <label>
          Password
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        </label>
        <button type="submit" disabled={submitting}>
          {submitting ? 'Signing in…' : 'Sign in'}
        </button>
      </form>
    </div>
  )
}
