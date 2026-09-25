export type UserRole = 'ADMIN' | 'DEVELOPER' | 'QA' | 'USER'
export type TicketStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED' | 'CANCELLED'
export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

export interface FieldError {
  field: string
  message: string
}

export interface ErrorResponse {
  code: string
  message: string
  fieldErrors?: FieldError[]
}

export interface UserSummary {
  id: number
  username: string
  displayName: string
}

export interface User {
  id: number | null
  username: string
  displayName: string
  email: string | null
  role: UserRole
}

export interface LoginResponse {
  token: string
  username: string
  displayName: string
  role: UserRole
}

export interface TicketSummary {
  id: number
  title: string
  status: TicketStatus
  priority: TicketPriority
  assignee: UserSummary | null
  createdBy: UserSummary
  createdAt: string
  updatedAt: string
}

export interface Comment {
  id: number
  body: string
  author: UserSummary
  createdAt: string
}

export interface TicketDetail extends TicketSummary {
  description: string
  comments: Comment[]
}

export interface StatusSection {
  status: TicketStatus
  tickets: TicketSummary[]
  totalElements: number
  totalPages: number
}

export interface GroupedTicketResponse {
  page: number
  size: number
  sections: StatusSection[]
}
