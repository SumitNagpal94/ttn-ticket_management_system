import { Link } from 'react-router-dom'
import type { TicketSummary } from '../../types/api'

interface TicketCardProps {
  ticket: TicketSummary
}

export function TicketCard({ ticket }: TicketCardProps) {
  return (
    <Link to={`/tickets/${ticket.id}`} className="ticket-card">
      <span className="ticket-title">{ticket.title}</span>
      <span className="ticket-meta">
        {ticket.priority} · {ticket.assignee?.displayName ?? 'Unassigned'}
      </span>
    </Link>
  )
}
