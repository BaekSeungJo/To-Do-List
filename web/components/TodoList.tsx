'use client';

import { TodoDto } from '../lib/todos';

interface TodoListProps {
  todos: TodoDto[];
  onToggle: (id: string) => Promise<void> | void;
  onDelete: (id: string) => Promise<void> | void;
}

export default function TodoList({ todos, onToggle, onDelete }: TodoListProps) {
  if (todos.length === 0) {
    return <p style={{ marginTop: '2rem', textAlign: 'center', color: '#6b7280' }}>등록된 할 일이 없습니다.</p>;
  }

  return (
    <ul style={{ listStyle: 'none', padding: 0, margin: '1.5rem 0 0', display: 'grid', gap: '0.75rem' }}>
      {todos.map((todo) => (
        <li
          key={todo.id}
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            background: '#fff',
            borderRadius: '0.75rem',
            padding: '1rem',
            boxShadow: '0 1px 2px rgba(15, 23, 42, 0.08)'
          }}
        >
          <div>
            <label style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={todo.done}
                onChange={() => onToggle(todo.id)}
              />
              <div>
                <p style={{ margin: 0, fontWeight: 600, textDecoration: todo.done ? 'line-through' : 'none' }}>{todo.title}</p>
                {todo.dueDate ? (
                  <small style={{ color: '#6b7280' }}>마감일: {new Date(todo.dueDate).toLocaleDateString()}</small>
                ) : null}
              </div>
            </label>
          </div>
          <button
            type="button"
            onClick={() => onDelete(todo.id)}
            style={{
              background: 'transparent',
              border: 'none',
              color: '#ef4444',
              cursor: 'pointer'
            }}
          >
            삭제
          </button>
        </li>
      ))}
    </ul>
  );
}
