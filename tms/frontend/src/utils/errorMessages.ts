const MESSAGES: Record<string, string> = {
  VALIDATION_ERROR: 'Please fix the highlighted fields.',
  UNAUTHORIZED: 'Please log in to continue.',
  FORBIDDEN: 'You do not have permission to perform this action.',
  NOT_FOUND: 'The requested item was not found.',
  INVALID_TRANSITION: 'This status change is not allowed.',
  INVALID_CREDENTIALS: 'Invalid username or password.',
  INVALID_FILE_TYPE: 'Only JPEG, PNG, GIF, WebP, and PDF files are allowed.',
  FILE_TOO_LARGE: 'File exceeds the 10 MB size limit.',
  ATTACHMENT_LIMIT_EXCEEDED: 'This ticket already has the maximum number of attachments.',
}

export function friendlyError(code: string, fallback?: string): string {
  return MESSAGES[code] ?? fallback ?? 'Something went wrong. Please try again.'
}
