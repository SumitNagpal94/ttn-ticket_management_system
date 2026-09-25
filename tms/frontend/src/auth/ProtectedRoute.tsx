import { Navigate } from 'react-router-dom'
import { LoadingSpinner } from '../components/LoadingSpinner'
import type { UserRole } from '../types/api'
import { useAuth } from './useAuth'

interface ProtectedRouteProps {
  children: React.ReactNode
  roles?: UserRole[]
}

export function ProtectedRoute({ children, roles }: ProtectedRouteProps) {
  const { user, loading } = useAuth()

  if (loading) return <LoadingSpinner />
  if (!user) return <Navigate to="/login" replace />
  if (roles && !roles.includes(user.role)) {
    return (
      <div className="page">
        <p>Access denied. You do not have permission to view this page.</p>
      </div>
    )
  }
  return children
}
