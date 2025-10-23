import api from './api';

export interface TodoDto {
  id: string;
  title: string;
  dueDate?: string;
  done: boolean;
  createdAt: string;
  updatedAt: string;
}

export type TodoStatusFilter = 'all' | 'active' | 'done';

export async function listTodos(status: TodoStatusFilter = 'all'): Promise<TodoDto[]> {
  const { data } = await api.get<TodoDto[]>('/todos', {
    params: { status }
  });
  return data;
}

export interface CreateTodoPayload {
  title: string;
  dueDate?: string;
}

export async function createTodo(payload: CreateTodoPayload): Promise<TodoDto> {
  const { data } = await api.post<TodoDto>('/todos', payload);
  return data;
}

export interface UpdateTodoPayload {
  title: string;
  dueDate?: string;
}

export async function updateTodo(id: string, payload: UpdateTodoPayload): Promise<TodoDto> {
  const { data } = await api.patch<TodoDto>(`/todos/${id}`, payload);
  return data;
}

export async function toggleTodo(id: string): Promise<TodoDto> {
  const { data } = await api.patch<TodoDto>(`/todos/${id}/toggle`);
  return data;
}

export async function deleteTodo(id: string): Promise<void> {
  await api.delete(`/todos/${id}`);
}
