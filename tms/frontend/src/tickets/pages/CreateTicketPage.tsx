import { type FormEvent, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ErrorAlert } from '../../components/ErrorAlert'
import { ApiError } from '../../services/apiClient'
import * as ticketApi from '../../services/ticketApi'
import { friendlyError } from '../../utils/errorMessages'

export function CreateTicketPage() {
  const navigate = useNavigate()
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<{ field: string; message: string }[]>([])

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    try {
      const ticket = await ticketApi.createTicket(title, description)
      navigate(`/tickets/${ticket.id}`)
    } catch (err) {
      if (err instanceof ApiError) {
        setError(friendlyError(err.code, err.message))
        setFieldErrors(err.fieldErrors)
      }
    }
  }

  return (
    <div className="page">
      <h1>Create ticket</h1>
      <form className="card" onSubmit={handleSubmit}>
        {error && <ErrorAlert message={error} fieldErrors={fieldErrors} />}
        <label>Title<input value={title} onChange={(e) => setTitle(e.target.value)} required maxLength={200} /></label>
        <label>Description<textarea value={description} onChange={(e) => setDescription(e.target.value)} required rows={6} /></label>
        <button type="submit">Create</button>
      </form>
    </div>
  )
}
