const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? '';

export type TodoFilter = 'all' | 'active' | 'done';

export interface TodoDto {
  id: string;
  title: string;
  dueDate: string | null;
  done: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTodoInput {
  title: string;
  dueDate?: string | null;
}

export interface UpdateTodoInput {
  title?: string;
  dueDate?: string | null;
  done?: boolean;
}

export class ApiError extends Error {
  public readonly status: number;
  public readonly detail?: unknown;

  constructor(message: string, status: number, detail?: unknown) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.detail = detail;
  }
}

function resolveUrl(path: string, query?: Record<string, string | number | undefined>): string {
  const hasBase = API_BASE_URL.length > 0;
  const basePath = hasBase ? new URL(path, API_BASE_URL).toString() : path;
  if (!query) {
    return basePath;
  }

  const origin = hasBase
    ? undefined
    : typeof window !== 'undefined'
      ? window.location.origin
      : 'http://localhost';
  const url = new URL(basePath, origin);
  Object.entries(query).forEach(([key, value]) => {
    if (value !== undefined && value !== null) {
      url.searchParams.set(key, String(value));
    }
  });
  return url.toString();
}

async function request<T>(path: string, token: string, init?: RequestInit): Promise<T> {
  const headers = new Headers(init?.headers);
  headers.set('Accept', 'application/json');
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }
  const hasBody = init?.body !== undefined && init.body !== null;
  if (hasBody && !(init?.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }

  const response = await fetch(path, {
    ...init,
    headers
  });

  if (!response.ok) {
    let detail: unknown = undefined;
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      try {
        detail = await response.json();
      } catch (err) {
        detail = undefined;
      }
    } else {
      const text = await response.text();
      detail = text.length ? text : undefined;
    }
    throw new ApiError(response.statusText || 'Request failed', response.status, detail);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  if (!text) {
    return undefined as T;
  }
  return JSON.parse(text) as T;
}

export async function fetchTodos(filter: TodoFilter, token: string): Promise<TodoDto[]> {
  const url = resolveUrl('/api/todos', filter === 'all' ? undefined : { status: filter });
  return request<TodoDto[]>(url, token);
}

export async function createTodo(input: CreateTodoInput, token: string): Promise<TodoDto> {
  const url = resolveUrl('/api/todos');
  const payload: Record<string, unknown> = {
    title: input.title
  };
  if (input.dueDate !== undefined) {
    payload.dueDate = input.dueDate;
  }
  return request<TodoDto>(url, token, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export async function updateTodo(id: string, input: UpdateTodoInput, token: string): Promise<TodoDto> {
  const url = resolveUrl(`/api/todos/${id}`);
  const payload: Record<string, unknown> = {};
  if (Object.prototype.hasOwnProperty.call(input, 'title')) {
    payload.title = input.title;
  }
  if (Object.prototype.hasOwnProperty.call(input, 'dueDate')) {
    payload.dueDate = input.dueDate;
  }
  if (Object.prototype.hasOwnProperty.call(input, 'done')) {
    payload.done = input.done;
  }
  return request<TodoDto>(url, token, {
    method: 'PATCH',
    body: JSON.stringify(payload)
  });
}

export async function deleteTodo(id: string, token: string): Promise<void> {
  const url = resolveUrl(`/api/todos/${id}`);
  await request<void>(url, token, { method: 'DELETE' });
}
