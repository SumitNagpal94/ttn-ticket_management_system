interface SearchBarProps {
  value: string
  onChange: (value: string) => void
  onSearch: () => void
}

export function SearchBar({ value, onChange, onSearch }: SearchBarProps) {
  return (
    <div className="search-bar">
      <input
        type="search"
        placeholder="Search title or description…"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        onKeyDown={(e) => e.key === 'Enter' && onSearch()}
      />
      <button type="button" onClick={onSearch}>Search</button>
    </div>
  )
}
