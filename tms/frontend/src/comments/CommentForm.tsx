import { type FormEvent, useState } from 'react'
import { ErrorAlert } from '../components/ErrorAlert'
import { ApiError } from '../services/apiClient'
import * as ticketApi from '../services/ticketApi'
import { friendlyError } from '../utils/errorMessages'

interface CommentFormProps {
  ticketId: number
  onAdded: () => void
}

export function CommentForm({ ticketId, onAdded }: CommentFormProps) {
  const [body, setBody] = useState('')
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    try {
      await ticketApi.addComment(ticketId, body)
      setBody('')
      onAdded()
    } catch (err) {
      if (err instanceof ApiError) setError(friendlyError(err.code, err.message))
    }
  }

  return (
    <form className="comment-form" onSubmit={handleSubmit}>
      {error && <ErrorAlert message={error} />}
      <textarea value={body} onChange={(e) => setBody(e.target.value)} required rows={3} placeholder="Add a comment…" />
      <button type="submit">Post comment</button>
    </form>
  )
}
