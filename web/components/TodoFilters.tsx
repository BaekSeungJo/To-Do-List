'use client';

import { TodoStatusFilter } from '../lib/todos';

interface TodoFiltersProps {
  value: TodoStatusFilter;
  onChange: (value: TodoStatusFilter) => void;
}

const labels: Record<TodoStatusFilter, string> = {
  all: '전체',
  active: '미완료',
  done: '완료'
};

export default function TodoFilters({ value, onChange }: TodoFiltersProps) {
  return (
    <div style={{ display: 'flex', gap: '0.5rem' }}>
      {(Object.keys(labels) as TodoStatusFilter[]).map((status) => (
        <button
          key={status}
          type="button"
          onClick={() => onChange(status)}
          style={{
            padding: '0.4rem 0.8rem',
            borderRadius: '9999px',
            border: '1px solid #d1d5db',
            backgroundColor: value === status ? '#2563eb' : '#fff',
            color: value === status ? '#fff' : '#111827',
            cursor: 'pointer'
          }}
        >
          {labels[status]}
        </button>
      ))}
    </div>
  );
}
