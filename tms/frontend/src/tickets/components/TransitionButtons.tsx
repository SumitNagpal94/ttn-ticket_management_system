import type { TicketDetail, TicketStatus, UserRole } from '../../types/api'

interface TransitionButtonsProps {
  ticket: TicketDetail
  role: UserRole
  userId: number | null
  onTransition: (status: TicketStatus) => void
  disabled?: boolean
}

function allowedTargets(ticket: TicketDetail, role: UserRole, userId: number | null): TicketStatus[] {
  const current = ticket.status
  const map: Record<TicketStatus, TicketStatus[]> = {
    OPEN: ['IN_PROGRESS', 'CANCELLED'],
    IN_PROGRESS: ['RESOLVED', 'CANCELLED'],
    RESOLVED: ['CLOSED'],
    CLOSED: [],
    CANCELLED: [],
  }
  const targets = map[current] ?? []
  if (role === 'DEVELOPER' || role === 'QA') {
    return targets.filter((t) => t !== 'CLOSED' || role === 'QA' || (userId != null && ticket.createdBy.id === userId))
  }
  if (current === 'RESOLVED' && userId != null && ticket.createdBy.id === userId) {
    return ['CLOSED']
  }
  return []
}

export function TransitionButtons({ ticket, role, userId, onTransition, disabled }: TransitionButtonsProps) {
  const targets = allowedTargets(ticket, role, userId)
  if (targets.length === 0) return null

  return (
    <div className="transition-buttons">
      {targets.map((status) => (
        <button key={status} type="button" disabled={disabled} onClick={() => onTransition(status)}>
          → {status.replace('_', ' ')}
        </button>
      ))}
    </div>
  )
}
