import type { Attachment, User } from '../types/api'
import * as attachmentApi from '../services/attachmentApi'

function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

interface Props {
  ticketId: number
  attachments: Attachment[]
  user: User
  onDeleted: () => void
  onError: (message: string) => void
}

export function AttachmentList({ ticketId, attachments, user, onDeleted, onError }: Props) {
  if (attachments.length === 0) {
    return <p className="muted">No attachments yet.</p>
  }

  async function downloadFile(attachment: Attachment) {
    try {
      const token = localStorage.getItem('tms_token')
      const response = await fetch(
        attachmentApi.downloadAttachmentUrl(ticketId, attachment.id),
        { headers: token ? { Authorization: `Bearer ${token}` } : {} },
      )
      if (!response.ok) {
        onError('Download failed')
        return
      }
      const blob = await response.blob()
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = attachment.originalFilename
      link.click()
      URL.revokeObjectURL(url)
    } catch {
      onError('Download failed')
    }
  }

  async function handleDelete(attachment: Attachment) {
    try {
      await attachmentApi.deleteAttachment(ticketId, attachment.id)
      onDeleted()
    } catch (err) {
      onError(err instanceof Error ? err.message : 'Delete failed')
    }
  }

  return (
    <ul className="attachment-list">
      {attachments.map((a) => {
        const canDelete = user.role === 'ADMIN' || a.uploadedBy.id === user.id
        return (
          <li key={a.id} className="attachment-item">
            <button type="button" className="link-button" onClick={() => downloadFile(a)}>
              {a.originalFilename}
            </button>
            <span className="muted">
              {a.uploadedBy.displayName} · {formatSize(a.fileSize)} · {new Date(a.createdAt).toLocaleString()}
            </span>
            {canDelete && (
              <button type="button" className="link-button" onClick={() => handleDelete(a)}>
                Delete
              </button>
            )}
          </li>
        )
      })}
    </ul>
  )
}
