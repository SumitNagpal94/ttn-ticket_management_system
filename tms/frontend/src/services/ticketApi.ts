import type {
  Comment,
  GroupedTicketResponse,
  TicketDetail,
  TicketPriority,
  TicketStatus,
} from '../types/api'
import { apiRequest } from './apiClient'

export function createTicket(title: string, description: string): Promise<TicketDetail> {
  return apiRequest<TicketDetail>('/tickets', {
    method: 'POST',
    body: JSON.stringify({ title, description }),
  })
}

export function getGroupedTickets(params: {
  assigneeId?: number
  q?: string
  page?: number
}): Promise<GroupedTicketResponse> {
  const search = new URLSearchParams()
  if (params.assigneeId !== undefined) search.set('assigneeId', String(params.assigneeId))
  if (params.q) search.set('q', params.q)
  if (params.page !== undefined) search.set('page', String(params.page))
  const qs = search.toString()
  return apiRequest<GroupedTicketResponse>(`/tickets/grouped${qs ? `?${qs}` : ''}`)
}

export function getTicket(id: number): Promise<TicketDetail> {
  return apiRequest<TicketDetail>(`/tickets/${id}`)
}

export function updateTicket(
  id: number,
  payload: {
    title?: string
    description?: string
    priority?: TicketPriority
    assigneeId?: number | null
    clearAssignee?: boolean
  },
): Promise<TicketDetail> {
  return apiRequest<TicketDetail>(`/tickets/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  })
}

export function transitionTicket(id: number, targetStatus: TicketStatus): Promise<TicketDetail> {
  return apiRequest<TicketDetail>(`/tickets/${id}/transitions`, {
    method: 'POST',
    body: JSON.stringify({ targetStatus }),
  })
}

export function addComment(ticketId: number, body: string): Promise<Comment> {
  return apiRequest<Comment>(`/tickets/${ticketId}/comments`, {
    method: 'POST',
    body: JSON.stringify({ body }),
  })
}
