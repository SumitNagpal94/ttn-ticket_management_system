import { type FormEvent, useEffect, useState } from 'react'
import { ErrorAlert } from '../components/ErrorAlert'
import { LoadingSpinner } from '../components/LoadingSpinner'
import { ApiError } from '../services/apiClient'
import * as userApi from '../services/userApi'
import type { User, UserRole } from '../types/api'
import { friendlyError } from '../utils/errorMessages'

const ROLES: UserRole[] = ['DEVELOPER', 'QA', 'USER']

export function AdminUsersPage() {
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [form, setForm] = useState({
    username: '',
    password: '',
    displayName: '',
    email: '',
    role: 'USER' as UserRole,
  })

  async function load() {
    setLoading(true)
    try {
      const res = await userApi.listUsers()
      setUsers(res.content)
    } catch (err) {
      if (err instanceof ApiError) setError(friendlyError(err.code, err.message))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  async function handleCreate(e: FormEvent) {
    e.preventDefault()
    setError(null)
    try {
      await userApi.createUser(form)
      setForm({ username: '', password: '', displayName: '', email: '', role: 'USER' })
      await load()
    } catch (err) {
      if (err instanceof ApiError) setError(friendlyError(err.code, err.message))
    }
  }

  async function handleReset(id: number) {
    const password = window.prompt('New password (min 8 chars):')
    if (!password) return
    try {
      await userApi.resetPassword(id, password)
    } catch (err) {
      if (err instanceof ApiError) setError(friendlyError(err.code, err.message))
    }
  }

  async function handleRoleChange(id: number, role: UserRole) {
    try {
      await userApi.updateRole(id, role)
      await load()
    } catch (err) {
      if (err instanceof ApiError) setError(friendlyError(err.code, err.message))
    }
  }

  if (loading) return <LoadingSpinner />

  return (
    <div className="page">
      <h1>User management</h1>
      {error && <ErrorAlert message={error} />}
      <form className="card" onSubmit={handleCreate}>
        <h2>Create user</h2>
        <label>Username<input value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} required /></label>
        <label>Password<input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required minLength={8} /></label>
        <label>Display name<input value={form.displayName} onChange={(e) => setForm({ ...form, displayName: e.target.value })} required /></label>
        <label>Email<input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required /></label>
        <label>
          Role
          <select value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value as UserRole })}>
            {ROLES.map((r) => <option key={r} value={r}>{r}</option>)}
          </select>
        </label>
        <button type="submit">Create</button>
      </form>
      <table className="user-table">
        <thead>
          <tr><th>Username</th><th>Name</th><th>Email</th><th>Role</th><th>Actions</th></tr>
        </thead>
        <tbody>
          {users.map((u) => (
            <tr key={u.id ?? u.username}>
              <td>{u.username}</td>
              <td>{u.displayName}</td>
              <td>{u.email}</td>
              <td>
                <select value={u.role} onChange={(e) => u.id != null && handleRoleChange(u.id, e.target.value as UserRole)}>
                  {ROLES.map((r) => <option key={r} value={r}>{r}</option>)}
                </select>
              </td>
              <td><button type="button" onClick={() => u.id != null && handleReset(u.id)}>Reset password</button></td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
