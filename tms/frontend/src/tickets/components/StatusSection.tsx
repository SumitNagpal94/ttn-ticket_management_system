import type { StatusSection as StatusSectionType } from '../../types/api'
import { EmptyState } from '../../components/EmptyState'
import { TicketCard } from './TicketCard'

interface StatusSectionProps {
  section: StatusSectionType
}

const STATUS_LABELS: Record<string, string> = {
  OPEN: 'Open',
  IN_PROGRESS: 'In Progress',
  RESOLVED: 'Resolved',
  CLOSED: 'Closed',
  CANCELLED: 'Cancelled',
}

export function StatusSection({ section }: StatusSectionProps) {
  return (
    <section className="status-section">
      <h2>{STATUS_LABELS[section.status] ?? section.status} ({section.totalElements})</h2>
      {section.tickets.length === 0 ? (
        <EmptyState message="No tickets in this section." />
      ) : (
        <div className="ticket-list">
          {section.tickets.map((t) => <TicketCard key={t.id} ticket={t} />)}
        </div>
      )}
    </section>
  )
}
