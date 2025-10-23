'use client';

import { useState } from 'react';

interface TodoFormProps {
  onSubmit: (values: { title: string; dueDate?: string }) => Promise<void> | void;
  loading?: boolean;
}

export default function TodoForm({ onSubmit, loading }: TodoFormProps) {
  const [title, setTitle] = useState('');
  const [dueDate, setDueDate] = useState('');

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!title.trim()) {
      return;
    }
    await onSubmit({ title: title.trim(), dueDate: dueDate || undefined });
    setTitle('');
    setDueDate('');
  };

  return (
    <form onSubmit={handleSubmit} style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
      <input
        type="text"
        placeholder="할 일을 입력하세요"
        value={title}
        onChange={(event) => setTitle(event.target.value)}
        maxLength={100}
        style={{ flex: '1 1 240px', padding: '0.5rem', borderRadius: '0.375rem', border: '1px solid #d1d5db' }}
      />
      <input
        type="date"
        value={dueDate}
        onChange={(event) => setDueDate(event.target.value)}
        style={{ padding: '0.5rem', borderRadius: '0.375rem', border: '1px solid #d1d5db' }}
      />
      <button
        type="submit"
        disabled={loading}
        style={{
          backgroundColor: '#2563eb',
          color: '#fff',
          border: 'none',
          borderRadius: '0.375rem',
          padding: '0.5rem 1.25rem',
          cursor: 'pointer'
        }}
      >
        추가
      </button>
    </form>
  );
}
