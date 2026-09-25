import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../auth/useAuth'
import { ErrorAlert } from '../../components/ErrorAlert'
import { LoadingSpinner } from '../../components/LoadingSpinner'
import { ApiError } from '../../services/apiClient'
import * as ticketApi from '../../services/ticketApi'
import * as userApi from '../../services/userApi'
import type { GroupedTicketResponse, UserSummary } from '../../types/api'
import { friendlyError } from '../../utils/errorMessages'
import { AssigneeFilter } from '../components/AssigneeFilter'
import { SearchBar } from '../components/SearchBar'
import { StatusSection } from '../components/StatusSection'

export function TicketListPage() {
  const { user } = useAuth()
  const [data, setData] = useState<GroupedTicketResponse | null>(null)
  const [assignees, setAssignees] = useState<UserSummary[]>([])
  const [assigneeId, setAssigneeId] = useState('')
  const [assigneeReady, setAssigneeReady] = useState(false)
  const [query, setQuery] = useState('')
  const [searchInput, setSearchInput] = useState('')
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (assigneeReady || !user) return
    if (user.id != null) {
      setAssigneeId(String(user.id))
    }
    setAssigneeReady(true)
  }, [user, assigneeReady])

  const load = useCallback(async () => {
    if (!assigneeReady) return
    setLoading(true)
    setError(null)
    try {
      const res = await ticketApi.getGroupedTickets({
        assigneeId: assigneeId ? Number(assigneeId) : undefined,
        q: query || undefined,
        page,
      })
      setData(res)
    } catch (err) {
      if (err instanceof ApiError) setError(friendlyError(err.code, err.message))
    } finally {
      setLoading(false)
    }
  }, [assigneeId, assigneeReady, query, page])

  useEffect(() => {
    userApi.listAssignees().then(setAssignees).catch(() => {})
  }, [])

  useEffect(() => {
    load()
  }, [load])

  if (loading && !data) return <LoadingSpinner />

  const hasNextPage = data?.sections.some((s) => page + 1 < s.totalPages) ?? false

  return (
    <div className="page">
      <header className="page-header">
        <h1>Tickets</h1>
        <Link to="/tickets/new" className="btn-primary">New ticket</Link>
      </header>
      {error && <ErrorAlert message={error} />}
      <div className="filters">
        <SearchBar value={searchInput} onChange={setSearchInput} onSearch={() => { setQuery(searchInput); setPage(0) }} />
        <AssigneeFilter assignees={assignees} value={assigneeId} onChange={(v) => { setAssigneeId(v); setPage(0) }} />
      </div>
      {data?.sections.map((s) => <StatusSection key={s.status} section={s} />)}
      <div className="pagination">
        <button type="button" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>Previous</button>
        <span>Page {page + 1}</span>
        <button type="button" disabled={!hasNextPage} onClick={() => setPage((p) => p + 1)}>Next</button>
      </div>
    </div>
  )
}
