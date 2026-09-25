interface ErrorAlertProps {
  message: string
  fieldErrors?: { field: string; message: string }[]
}

export function ErrorAlert({ message, fieldErrors }: ErrorAlertProps) {
  return (
    <div className="error-alert" role="alert">
      <p>{message}</p>
      {fieldErrors && fieldErrors.length > 0 && (
        <ul>
          {fieldErrors.map((e) => (
            <li key={e.field}>{e.field}: {e.message}</li>
          ))}
        </ul>
      )}
    </div>
  )
}
