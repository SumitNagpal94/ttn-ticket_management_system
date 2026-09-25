import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import type { Attachment, User } from '../types/api'
import * as attachmentApi from '../services/attachmentApi'
import { AttachmentList } from './AttachmentList'

const attachment: Attachment = {
  id: 10,
  originalFilename: 'screenshot.png',
  contentType: 'image/png',
  fileSize: 2048,
  uploadedBy: { id: 2, displayName: 'Alice' },
  createdAt: '2026-01-15T10:00:00Z',
}

const uploader: User = {
  id: 2,
  username: 'alice',
  displayName: 'Alice',
  email: 'alice@example.com',
  role: 'USER',
}

const otherUser: User = {
  id: 3,
  username: 'bob',
  displayName: 'Bob',
  email: 'bob@example.com',
  role: 'USER',
}

describe('AttachmentList', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('shows empty state when there are no attachments', () => {
    render(
      <AttachmentList
        ticketId={1}
        attachments={[]}
        user={uploader}
        onDeleted={vi.fn()}
        onError={vi.fn()}
      />,
    )
    expect(screen.getByText('No attachments yet.')).toBeTruthy()
  })

  it('allows uploader to delete own attachment', async () => {
    const onDeleted = vi.fn()
    vi.spyOn(attachmentApi, 'deleteAttachment').mockResolvedValue()

    render(
      <AttachmentList
        ticketId={1}
        attachments={[attachment]}
        user={uploader}
        onDeleted={onDeleted}
        onError={vi.fn()}
      />,
    )

    fireEvent.click(screen.getByRole('button', { name: 'Delete' }))
    await waitFor(() => expect(onDeleted).toHaveBeenCalled())
    expect(attachmentApi.deleteAttachment).toHaveBeenCalledWith(1, 10)
  })

  it('hides delete for other users', () => {
    render(
      <AttachmentList
        ticketId={1}
        attachments={[attachment]}
        user={otherUser}
        onDeleted={vi.fn()}
        onError={vi.fn()}
      />,
    )

    expect(screen.queryByRole('button', { name: 'Delete' })).toBeNull()
  })

  it('shows delete for admin on any attachment', () => {
    const admin: User = { ...otherUser, role: 'ADMIN' }
    render(
      <AttachmentList
        ticketId={1}
        attachments={[attachment]}
        user={admin}
        onDeleted={vi.fn()}
        onError={vi.fn()}
      />,
    )

    expect(screen.getByRole('button', { name: 'Delete' })).toBeTruthy()
  })
})
