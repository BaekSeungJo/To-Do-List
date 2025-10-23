'use client';

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import TodoFilters from '../../components/TodoFilters';
import TodoForm from '../../components/TodoForm';
import TodoList from '../../components/TodoList';
import {
  TodoStatusFilter,
  createTodo,
  deleteTodo,
  listTodos,
  toggleTodo
} from '../../lib/todos';

export default function TodosPage() {
  const [status, setStatus] = useState<TodoStatusFilter>('all');
  const queryClient = useQueryClient();

  const todosQuery = useQuery({
    queryKey: ['todos', status],
    queryFn: () => listTodos(status)
  });

  const createMutation = useMutation({
    mutationFn: createTodo,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['todos'] })
  });

  const toggleMutation = useMutation({
    mutationFn: toggleTodo,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['todos'] })
  });

  const deleteMutation = useMutation({
    mutationFn: deleteTodo,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['todos'] })
  });

  const handleCreate = async (values: { title: string; dueDate?: string }) => {
    await createMutation.mutateAsync(values);
  };

  const handleToggle = async (id: string) => {
    await toggleMutation.mutateAsync(id);
  };

  const handleDelete = async (id: string) => {
    await deleteMutation.mutateAsync(id);
  };

  return (
    <main style={{ margin: '0 auto', maxWidth: 720, padding: '2rem 1.5rem' }}>
      <header style={{ marginBottom: '2rem' }}>
        <h1 style={{ marginBottom: '0.75rem' }}>내 할 일</h1>
        <p style={{ margin: 0, color: '#6b7280' }}>Firebase 로그인 기반 개인용 To-Do 서비스</p>
      </header>

      <section style={{ display: 'grid', gap: '1.5rem' }}>
        <TodoForm onSubmit={handleCreate} loading={createMutation.isPending} />
        <TodoFilters value={status} onChange={setStatus} />
      </section>

      {todosQuery.isLoading ? (
        <p style={{ marginTop: '2rem', textAlign: 'center' }}>불러오는 중...</p>
      ) : todosQuery.isError ? (
        <p style={{ marginTop: '2rem', textAlign: 'center', color: '#ef4444' }}>
          데이터를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.
        </p>
      ) : (
        <TodoList todos={todosQuery.data ?? []} onToggle={handleToggle} onDelete={handleDelete} />
      )}
    </main>
  );
}
