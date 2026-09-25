import { useCallback, useEffect, useState } from 'react'
import { ApiError } from '../../services/apiClient'
import * as ticketApi from '../../services/ticketApi'
import type { TicketDetail } from '../../types/api'
import { friendlyError } from '../../utils/errorMessages'

export function useTicketDetail(id: number) {
  const [ticket, setTicket] = useState<TicketDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const reload = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await ticketApi.getTicket(id)
      setTicket(data)
    } catch (err) {
      if (err instanceof ApiError) setError(friendlyError(err.code, err.message))
    } finally {
      setLoading(false)
    }
  }, [id])

  useEffect(() => {
    reload()
  }, [reload])

  return { ticket, loading, error, reload }
}
