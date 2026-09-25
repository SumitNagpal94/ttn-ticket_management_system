import { afterEach, describe, expect, it, vi } from 'vitest'
import * as attachmentApi from './attachmentApi'

describe('attachmentApi', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('uploads multipart file to ticket attachments endpoint', async () => {
    const file = new File(['%PDF-1.4'], 'doc.pdf', { type: 'application/pdf' })
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      status: 201,
      text: async () => JSON.stringify({
        id: 1,
        originalFilename: 'doc.pdf',
        contentType: 'application/pdf',
        fileSize: 8,
        uploadedBy: { id: 2, displayName: 'User' },
        createdAt: '2026-01-01T00:00:00Z',
      }),
    }))

    const result = await attachmentApi.uploadAttachment(5, file)

    expect(result.originalFilename).toBe('doc.pdf')
    const [url, options] = vi.mocked(fetch).mock.calls[0]
    expect(url).toContain('/tickets/5/attachments')
    expect(options?.method).toBe('POST')
    expect(options?.body).toBeInstanceOf(FormData)
  })

  it('builds download URL for attachment', () => {
    expect(attachmentApi.downloadAttachmentUrl(3, 7)).toContain('/tickets/3/attachments/7')
  })
})
