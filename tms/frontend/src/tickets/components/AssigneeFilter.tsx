import type { UserSummary } from '../../types/api'

interface AssigneeFilterProps {
  assignees: UserSummary[]
  value: string
  onChange: (value: string) => void
}

export function AssigneeFilter({ assignees, value, onChange }: AssigneeFilterProps) {
  return (
    <label className="assignee-filter">
      Assignee
      <select value={value} onChange={(e) => onChange(e.target.value)}>
        <option value="">All assignees</option>
        {assignees.map((a) => (
          <option key={a.id} value={String(a.id)}>{a.displayName}</option>
        ))}
      </select>
    </label>
  )
}
