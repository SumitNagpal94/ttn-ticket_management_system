import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../services/apiClient'
import * as attachmentApi from '../services/attachmentApi'
import { AttachmentUpload } from './AttachmentUpload'

describe('AttachmentUpload', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('uploads selected file and notifies parent', async () => {
    const onUploaded = vi.fn()
    vi.spyOn(attachmentApi, 'uploadAttachment').mockResolvedValue({
      id: 1,
      originalFilename: 'shot.png',
      contentType: 'image/png',
      fileSize: 100,
      uploadedBy: { id: 2, displayName: 'Alice' },
      createdAt: '2026-01-01T00:00:00Z',
    })

    render(<AttachmentUpload ticketId={4} onUploaded={onUploaded} />)
    const input = screen.getByLabelText(/Upload file/i) as HTMLInputElement
    const file = new File(['png'], 'shot.png', { type: 'image/png' })

    fireEvent.change(input, { target: { files: [file] } })

    await waitFor(() => expect(onUploaded).toHaveBeenCalled())
    expect(attachmentApi.uploadAttachment).toHaveBeenCalledWith(4, file)
  })

  it('shows friendly message for invalid file type', async () => {
    vi.spyOn(attachmentApi, 'uploadAttachment').mockRejectedValue(
      new ApiError({ code: 'INVALID_FILE_TYPE', message: 'bad type', fieldErrors: [] }),
    )

    render(<AttachmentUpload ticketId={4} onUploaded={vi.fn()} />)
    const input = screen.getByLabelText(/Upload file/i) as HTMLInputElement
    fireEvent.change(input, { target: { files: [new File(['x'], 'bad.exe', { type: 'application/octet-stream' })] } })

    await waitFor(() => {
      expect(screen.getByText('Only JPEG, PNG, GIF, WebP, and PDF files are allowed.')).toBeTruthy()
    })
  })
})
