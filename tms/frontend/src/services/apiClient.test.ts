import { afterEach, describe, expect, it, vi } from 'vitest'
import { ApiError, apiRequest } from './apiClient'

describe('apiClient', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('throws ApiError with parsed body on failure', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: false,
      status: 400,
      text: async () => JSON.stringify({
        code: 'VALIDATION_ERROR',
        message: 'Invalid input',
        fieldErrors: [{ field: 'title', message: 'required' }],
      }),
    }))

    await expect(apiRequest('/tickets')).rejects.toMatchObject({
      code: 'VALIDATION_ERROR',
      message: 'Invalid input',
    })
    await expect(apiRequest('/tickets')).rejects.toBeInstanceOf(ApiError)
  })
})
