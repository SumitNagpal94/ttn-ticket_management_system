import { type FormEvent, useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { CommentForm } from '../../comments/CommentForm'
import { CommentList } from '../../comments/CommentList'
import { ErrorAlert } from '../../components/ErrorAlert'
import { LoadingSpinner } from '../../components/LoadingSpinner'
import { useAuth } from '../../auth/useAuth'
import { ApiError } from '../../services/apiClient'
import * as ticketApi from '../../services/ticketApi'
import * as userApi from '../../services/userApi'
import type { TicketPriority, UserSummary } from '../../types/api'
import { friendlyError } from '../../utils/errorMessages'
import { TransitionButtons } from '../components/TransitionButtons'
import { useTicketDetail } from '../hooks/useTicketDetail'

const PRIORITIES: TicketPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']

export function TicketDetailPage() {
  const { id } = useParams()
  const ticketId = Number(id)
  const { user } = useAuth()
  const { ticket, loading, error, reload } = useTicketDetail(ticketId)
  const [assignees, setAssignees] = useState<UserSummary[]>([])
  const [saveError, setSaveError] = useState<string | null>(null)
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [priority, setPriority] = useState<TicketPriority>('MEDIUM')
  const [assigneeId, setAssigneeId] = useState('')

  useEffect(() => {
    userApi.listAssignees().then(setAssignees).catch(() => {})
  }, [])

  useEffect(() => {
    if (ticket) {
      setTitle(ticket.title)
      setDescription(ticket.description)
      setPriority(ticket.priority)
      setAssigneeId(ticket.assignee ? String(ticket.assignee.id) : '')
    }
  }, [ticket])

  async function handleSave(e: FormEvent) {
    e.preventDefault()
    setSaveError(null)
    try {
      await ticketApi.updateTicket(ticketId, {
        title,
        description,
        priority,
        assigneeId: assigneeId ? Number(assigneeId) : undefined,
        clearAssignee: !assigneeId,
      })
      await reload()
    } catch (err) {
      if (err instanceof ApiError) setSaveError(friendlyError(err.code, err.message))
    }
  }

  async function handleTransition(targetStatus: Parameters<typeof ticketApi.transitionTicket>[1]) {
    setSaveError(null)
    try {
      await ticketApi.transitionTicket(ticketId, targetStatus)
      await reload()
    } catch (err) {
      if (err instanceof ApiError) setSaveError(friendlyError(err.code, err.message))
    }
  }

  if (loading) return <LoadingSpinner />
  if (error) return <ErrorAlert message={error} />
  if (!ticket || !user) return null

  return (
    <div className="page">
      <h1>Ticket #{ticket.id}</h1>
      <p className="status-badge">{ticket.status}</p>
      {saveError && <ErrorAlert message={saveError} />}
      <TransitionButtons
        ticket={ticket}
        role={user.role}
        userId={user.id}
        onTransition={handleTransition}
      />
      <form className="card" onSubmit={handleSave}>
        <label>Title<input value={title} onChange={(e) => setTitle(e.target.value)} required /></label>
        <label>Description<textarea value={description} onChange={(e) => setDescription(e.target.value)} required rows={6} /></label>
        <label>
          Priority
          <select value={priority} onChange={(e) => setPriority(e.target.value as TicketPriority)}>
            {PRIORITIES.map((p) => <option key={p} value={p}>{p}</option>)}
          </select>
        </label>
        <label>
          Assignee
          <select value={assigneeId} onChange={(e) => setAssigneeId(e.target.value)}>
            <option value="">Unassigned</option>
            {assignees.map((a) => <option key={a.id} value={String(a.id)}>{a.displayName}</option>)}
          </select>
        </label>
        <button type="submit">Save changes</button>
      </form>
      <section>
        <h2>Comments</h2>
        <CommentList comments={ticket.comments} />
        <CommentForm ticketId={ticketId} onAdded={reload} />
      </section>
    </div>
  )
}
