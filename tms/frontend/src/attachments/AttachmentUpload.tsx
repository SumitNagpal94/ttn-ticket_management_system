import { type ChangeEvent, useState } from 'react'
import { ApiError } from '../services/apiClient'
import * as attachmentApi from '../services/attachmentApi'
import { friendlyError } from '../utils/errorMessages'

interface Props {
  ticketId: number
  onUploaded: () => void
}

export function AttachmentUpload({ ticketId, onUploaded }: Props) {
  const [error, setError] = useState<string | null>(null)
  const [uploading, setUploading] = useState(false)

  async function handleChange(e: ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file) return
    setError(null)
    setUploading(true)
    try {
      await attachmentApi.uploadAttachment(ticketId, file)
      onUploaded()
    } catch (err) {
      if (err instanceof ApiError) {
        setError(friendlyError(err.code, err.message))
      } else {
        setError('Upload failed')
      }
    } finally {
      setUploading(false)
    }
  }

  return (
    <div className="attachment-upload">
      <label>
        Upload file (images or PDF, max 10 MB)
        <input type="file" accept=".jpg,.jpeg,.png,.gif,.webp,.pdf,image/*,application/pdf" onChange={handleChange} disabled={uploading} />
      </label>
      {uploading && <p className="muted">Uploading…</p>}
      {error && <p className="error-text">{error}</p>}
    </div>
  )
}
