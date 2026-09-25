import type { Comment } from '../types/api'

interface CommentListProps {
  comments: Comment[]
}

export function CommentList({ comments }: CommentListProps) {
  if (comments.length === 0) {
    return <p className="empty-state">No comments yet.</p>
  }
  return (
    <ul className="comment-list">
      {comments.map((c) => (
        <li key={c.id}>
          <strong>{c.author.displayName}</strong>
          <time>{new Date(c.createdAt).toLocaleString()}</time>
          <p>{c.body}</p>
        </li>
      ))}
    </ul>
  )
}
