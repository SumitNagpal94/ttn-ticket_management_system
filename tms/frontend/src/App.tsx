import { BrowserRouter, Link, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import { LoginPage } from './auth/LoginPage'
import { ProtectedRoute } from './auth/ProtectedRoute'
import { useAuth } from './auth/useAuth'
import { CreateTicketPage } from './tickets/pages/CreateTicketPage'
import { TicketDetailPage } from './tickets/pages/TicketDetailPage'
import { TicketListPage } from './tickets/pages/TicketListPage'
import { AdminUsersPage } from './users/AdminUsersPage'

function Layout({ children }: { children: React.ReactNode }) {
  const { user, logout } = useAuth()
  return (
    <div className="app">
      <nav className="nav">
        <Link to="/">Tickets</Link>
        {user?.role === 'ADMIN' && <Link to="/admin/users">Admin</Link>}
        {user && (
          <span className="nav-user">
            {user.displayName} ({user.role})
            <button type="button" onClick={logout}>Logout</button>
          </span>
        )}
      </nav>
      <main>{children}</main>
    </div>
  )
}

function AppRoutes() {
  const { user, loading } = useAuth()
  if (loading) return null

  return (
    <Routes>
      <Route path="/login" element={user ? <Navigate to="/" replace /> : <LoginPage />} />
      <Route path="/" element={<ProtectedRoute><Layout><TicketListPage /></Layout></ProtectedRoute>} />
      <Route path="/tickets/new" element={<ProtectedRoute><Layout><CreateTicketPage /></Layout></ProtectedRoute>} />
      <Route path="/tickets/:id" element={<ProtectedRoute><Layout><TicketDetailPage /></Layout></ProtectedRoute>} />
      <Route path="/admin/users" element={
        <ProtectedRoute roles={['ADMIN']}>
          <Layout><AdminUsersPage /></Layout>
        </ProtectedRoute>
      } />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  )
}
