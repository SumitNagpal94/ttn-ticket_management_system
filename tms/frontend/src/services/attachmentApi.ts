import { apiRequest, apiUpload } from './apiClient'
import type { Attachment } from '../types/api'

export function listAttachments(ticketId: number): Promise<Attachment[]> {
  return apiRequest<Attachment[]>(`/tickets/${ticketId}/attachments`)
}

export function uploadAttachment(ticketId: number, file: File): Promise<Attachment> {
  return apiUpload<Attachment>(`/tickets/${ticketId}/attachments`, file)
}

export function deleteAttachment(ticketId: number, attachmentId: number): Promise<void> {
  return apiRequest<void>(`/tickets/${ticketId}/attachments/${attachmentId}`, { method: 'DELETE' })
}

export function downloadAttachmentUrl(ticketId: number, attachmentId: number): string {
  const base = import.meta.env.VITE_API_BASE_URL ?? '/api'
  return `${base}/tickets/${ticketId}/attachments/${attachmentId}`
}
