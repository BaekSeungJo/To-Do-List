'use client';

import { useMemo, useState, useEffect, type FormEvent } from 'react';
import {
  createUserWithEmailAndPassword,
  GoogleAuthProvider,
  signInWithEmailAndPassword,
  signInWithPopup
} from 'firebase/auth';
import { FirebaseError } from 'firebase/app';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  ApiError,
  createTodo,
  deleteTodo,
  fetchTodos,
  type TodoDto,
  type TodoFilter,
  updateTodo
} from '../../lib/api-client';
import { getFirebaseAuth } from '../../lib/firebase';
import { useAuth } from '../../lib/auth-context';

const FILTERS: TodoFilter[] = ['all', 'active', 'done'];

type ToggleVariables = {
  todo: TodoDto;
  filterKey: TodoFilter;
};

type DeleteVariables = {
  id: string;
  filterKey: TodoFilter;
};

type UpdateVariables = {
  id: string;
  title?: string;
  dueDate?: string | null;
};

function getErrorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    if (error.status === 401) {
      return 'Your session has expired. Please sign in again.';
    }
    if (error.detail && typeof error.detail === 'object') {
      const detail = error.detail as Record<string, unknown>;
      if (typeof detail.message === 'string') {
        return detail.message;
      }
    }
    return error.message;
  }
  if (error instanceof FirebaseError) {
    switch (error.code) {
      case 'auth/invalid-email':
        return 'The email address is invalid.';
      case 'auth/user-disabled':
        return 'This account has been disabled.';
      case 'auth/user-not-found':
      case 'auth/wrong-password':
        return 'Incorrect email or password. Please try again.';
      case 'auth/email-already-in-use':
        return 'An account with this email already exists. Try signing in instead.';
      case 'auth/popup-closed-by-user':
        return 'The sign-in popup was closed before completing.';
      default:
        return error.message;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return 'Something went wrong. Please try again.';
}

function formatDate(date: string | null): string {
  if (!date) {
    return 'No due date';
  }
  try {
    return new Date(date).toLocaleDateString();
  } catch (error) {
    console.warn('Failed to format date', error);
    return date;
  }
}

export default function TodosPage(): JSX.Element {
  const firebaseAuth = useMemo(() => getFirebaseAuth(), []);
  const queryClient = useQueryClient();
  const { user, loading: authLoading, getIdToken, signOut } = useAuth();

  const [filter, setFilter] = useState<TodoFilter>('all');
  const [newTitle, setNewTitle] = useState('');
  const [newDueDate, setNewDueDate] = useState('');
  const [formError, setFormError] = useState<string | null>(null);
  const [authError, setAuthError] = useState<string | null>(null);
  const [authMode, setAuthMode] = useState<'signin' | 'signup'>('signin');
  const [authSubmitting, setAuthSubmitting] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editTitle, setEditTitle] = useState('');
  const [editDueDate, setEditDueDate] = useState('');

  const todosQuery = useQuery({
    queryKey: ['todos', filter],
    queryFn: async () => {
      const token = await getIdToken();
      if (!token) {
        throw new ApiError('Authentication required', 401);
      }
      return fetchTodos(filter, token);
    },
    enabled: !!user,
    staleTime: 30_000,
    retry: (failureCount, error) => {
      if (error instanceof ApiError && error.status === 401) {
        return false;
      }
      return failureCount < 2;
    }
  });

  const { data: todos = [], isLoading: todosLoading, error: todosError } = todosQuery;

  useEffect(() => {
    if (todosError instanceof ApiError && todosError.status === 401) {
      void signOut();
    }
  }, [signOut, todosError]);

  const createMutation = useMutation({
    mutationFn: async (input: { title: string; dueDate?: string | null }) => {
      const token = await getIdToken();
      if (!token) {
        throw new ApiError('Authentication required', 401);
      }
      return createTodo(input, token);
    },
    onSuccess: (created) => {
      setNewTitle('');
      setNewDueDate('');
      setFormError(null);
      if (filter !== 'done') {
        queryClient.setQueryData<TodoDto[]>(['todos', filter], (prev) => {
          const next = [created, ...(prev ?? [])];
          if (filter === 'active') {
            return next.filter((todo) => !todo.done);
          }
          return next;
        });
      }
      queryClient.invalidateQueries({ queryKey: ['todos'] });
    },
    onError: (error) => {
      setFormError(getErrorMessage(error));
    }
  });

  const toggleMutation = useMutation({
    mutationFn: async ({ todo }: ToggleVariables) => {
      const token = await getIdToken();
      if (!token) {
        throw new ApiError('Authentication required', 401);
      }
      return updateTodo(todo.id, { done: !todo.done }, token);
    },
    onMutate: async ({ todo, filterKey }) => {
      await queryClient.cancelQueries({ queryKey: ['todos', filterKey] });
      const previous = queryClient.getQueryData<TodoDto[]>(['todos', filterKey]);
      const nextDone = !todo.done;
      const updatedAt = new Date().toISOString();
      queryClient.setQueryData<TodoDto[]>(['todos', filterKey], (prev) => {
        if (!prev) {
          return prev;
        }
        const updated = prev.map((item) =>
          item.id === todo.id ? { ...item, done: nextDone, updatedAt } : item
        );
        if (filterKey === 'active') {
          return updated.filter((item) => !item.done);
        }
        if (filterKey === 'done') {
          return updated.filter((item) => item.done);
        }
        return updated;
      });
      return { previous, filterKey };
    },
    onError: (_error, _variables, context) => {
      if (!context) {
        return;
      }
      queryClient.setQueryData(['todos', context.filterKey], context.previous);
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['todos'] });
    }
  });

  const deleteMutation = useMutation({
    mutationFn: async ({ id }: DeleteVariables) => {
      const token = await getIdToken();
      if (!token) {
        throw new ApiError('Authentication required', 401);
      }
      await deleteTodo(id, token);
    },
    onMutate: async ({ id, filterKey }) => {
      await queryClient.cancelQueries({ queryKey: ['todos', filterKey] });
      const previous = queryClient.getQueryData<TodoDto[]>(['todos', filterKey]);
      queryClient.setQueryData<TodoDto[]>(['todos', filterKey], (prev) =>
        prev?.filter((todo) => todo.id !== id) ?? prev
      );
      return { previous, filterKey };
    },
    onError: (_error, _variables, context) => {
      if (!context) {
        return;
      }
      queryClient.setQueryData(['todos', context.filterKey], context.previous);
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['todos'] });
    }
  });

  const updateMutation = useMutation({
    mutationFn: async ({ id, title, dueDate }: UpdateVariables) => {
      const token = await getIdToken();
      if (!token) {
        throw new ApiError('Authentication required', 401);
      }
      return updateTodo(id, { title, dueDate }, token);
    },
    onSuccess: (updated) => {
      setEditingId(null);
      setEditTitle('');
      setEditDueDate('');
      FILTERS.forEach((filterKey) => {
        queryClient.setQueryData<TodoDto[]>(['todos', filterKey], (prev) => {
          if (!prev) {
            return prev;
          }
          const next = prev.map((item) => (item.id === updated.id ? updated : item));
          if (filterKey === 'active') {
            return next.filter((item) => !item.done);
          }
          if (filterKey === 'done') {
            return next.filter((item) => item.done);
          }
          return next;
        });
      });
      queryClient.invalidateQueries({ queryKey: ['todos'] });
    },
    onError: (error) => {
      setFormError(getErrorMessage(error));
    }
  });

  const handleCreateTodo = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const title = newTitle.trim();
    if (!title) {
      setFormError('Please enter a title before adding a todo.');
      return;
    }
    createMutation.mutate({
      title,
      dueDate: newDueDate ? newDueDate : null
    });
  };

  const handleToggle = (todo: TodoDto) => {
    toggleMutation.mutate({ todo, filterKey: filter });
  };

  const handleDelete = (id: string) => {
    deleteMutation.mutate({ id, filterKey: filter });
  };

  const startEditing = (todo: TodoDto) => {
    setEditingId(todo.id);
    setEditTitle(todo.title);
    setEditDueDate(todo.dueDate ?? '');
    setFormError(null);
  };

  const cancelEditing = () => {
    setEditingId(null);
    setEditTitle('');
    setEditDueDate('');
  };

  const submitEditing = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!editingId) {
      return;
    }
    const trimmedTitle = editTitle.trim();
    if (!trimmedTitle) {
      setFormError('Title cannot be empty.');
      return;
    }
    updateMutation.mutate({
      id: editingId,
      title: trimmedTitle,
      dueDate: editDueDate ? editDueDate : null
    });
  };

  const handleAuthSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);
    const email = String(formData.get('email') ?? '').trim();
    const password = String(formData.get('password') ?? '');
    if (!email || !password) {
      setAuthError('Email and password are required.');
      return;
    }
    setAuthError(null);
    setAuthSubmitting(true);
    try {
      if (authMode === 'signin') {
        await signInWithEmailAndPassword(firebaseAuth, email, password);
      } else {
        await createUserWithEmailAndPassword(firebaseAuth, email, password);
      }
    } catch (error) {
      setAuthError(getErrorMessage(error));
    } finally {
      setAuthSubmitting(false);
    }
  };

  const handleGoogleAuth = async () => {
    setAuthError(null);
    setAuthSubmitting(true);
    try {
      const provider = new GoogleAuthProvider();
      provider.setCustomParameters({ prompt: 'select_account' });
      await signInWithPopup(firebaseAuth, provider);
    } catch (error) {
      setAuthError(getErrorMessage(error));
    } finally {
      setAuthSubmitting(false);
    }
  };

  const handleSignOut = async () => {
    try {
      await signOut();
    } catch (error) {
      setFormError(getErrorMessage(error));
    }
  };

  const sortedTodos = useMemo(() => {
    return [...todos].sort(
      (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    );
  }, [todos]);

  const listError = todosError ? getErrorMessage(todosError) : null;

  if (authLoading) {
    return (
      <section className="flex min-h-screen items-center justify-center bg-slate-900 text-slate-200">
        <div className="rounded border border-slate-700 bg-slate-950 px-6 py-4 text-lg">
          Checking your authentication session...
        </div>
      </section>
    );
  }

  if (!user) {
    return (
      <section className="flex min-h-screen items-center justify-center bg-slate-950 p-6 text-slate-100">
        <div className="w-full max-w-md rounded-2xl border border-slate-800 bg-slate-900/70 p-6 shadow-xl backdrop-blur">
          <header className="mb-6 text-center">
            <h1 className="text-3xl font-semibold">Welcome back</h1>
            <p className="mt-2 text-sm text-slate-400">
              Sign in to manage your personal todo list securely with Firebase Authentication.
            </p>
          </header>
          <form onSubmit={handleAuthSubmit} className="flex flex-col gap-4">
            <div className="flex items-center justify-center gap-2 rounded-full bg-slate-800/60 p-1">
              <button
                type="button"
                className={`w-1/2 rounded-full px-4 py-2 text-sm font-semibold transition ${
                  authMode === 'signin'
                    ? 'bg-emerald-500 text-slate-900 shadow'
                    : 'text-slate-300 hover:text-white'
                }`}
                onClick={() => setAuthMode('signin')}
                disabled={authSubmitting}
              >
                Sign in
              </button>
              <button
                type="button"
                className={`w-1/2 rounded-full px-4 py-2 text-sm font-semibold transition ${
                  authMode === 'signup'
                    ? 'bg-sky-500 text-slate-900 shadow'
                    : 'text-slate-300 hover:text-white'
                }`}
                onClick={() => setAuthMode('signup')}
                disabled={authSubmitting}
              >
                Create account
              </button>
            </div>
            <label className="flex flex-col gap-2 text-sm font-medium">
              Email
              <input
                name="email"
                type="email"
                placeholder="you@example.com"
                className="rounded-md border border-slate-700 bg-slate-950 px-3 py-2 text-base text-slate-100 shadow focus:border-emerald-400 focus:outline-none focus:ring-2 focus:ring-emerald-400/40"
                autoComplete="email"
                disabled={authSubmitting}
                required
              />
            </label>
            <label className="flex flex-col gap-2 text-sm font-medium">
              Password
              <input
                name="password"
                type="password"
                placeholder="Enter at least 6 characters"
                className="rounded-md border border-slate-700 bg-slate-950 px-3 py-2 text-base text-slate-100 shadow focus:border-emerald-400 focus:outline-none focus:ring-2 focus:ring-emerald-400/40"
                autoComplete={authMode === 'signin' ? 'current-password' : 'new-password'}
                disabled={authSubmitting}
                required
                minLength={6}
              />
            </label>
            {authError ? (
              <div className="rounded-md border border-rose-500/60 bg-rose-500/10 px-3 py-2 text-sm text-rose-200">
                {authError}
              </div>
            ) : null}
            <button
              type="submit"
              className="flex items-center justify-center gap-2 rounded-md bg-emerald-500 px-4 py-2 text-base font-semibold text-slate-900 shadow transition hover:bg-emerald-400 disabled:cursor-not-allowed disabled:opacity-60"
              disabled={authSubmitting}
            >
              {authSubmitting ? 'Please wait…' : authMode === 'signin' ? 'Sign in' : 'Create account'}
            </button>
          </form>
          <div className="mt-6 flex flex-col gap-3">
            <div className="flex items-center gap-2 text-xs uppercase tracking-wide text-slate-500">
              <span className="grow border-t border-slate-700"></span>
              <span>or</span>
              <span className="grow border-t border-slate-700"></span>
            </div>
            <button
              type="button"
              onClick={handleGoogleAuth}
              className="flex items-center justify-center gap-2 rounded-md border border-slate-700 bg-slate-950 px-4 py-2 text-sm font-semibold text-slate-100 transition hover:border-slate-500 hover:bg-slate-900 disabled:cursor-not-allowed disabled:opacity-60"
              disabled={authSubmitting}
            >
              <svg className="h-5 w-5" viewBox="0 0 533.5 544.3" aria-hidden="true">
                <path
                  fill="#4285f4"
                  d="M533.5 278.4c0-17.4-1.5-34.1-4.5-50.3H272v95.1h146.9c-6.3 33.7-25.1 62.2-53.5 81.2v67.4h86.5c50.7-46.6 81.6-115.3 81.6-193.4z"
                />
                <path
                  fill="#34a853"
                  d="M272 544.3c72.9 0 134.1-24.1 178.8-65.4l-86.5-67.4c-24.1 16.3-55 26-92.3 26-71 0-131.1-47.9-152.6-112.4H30.2v70.2C74.3 482.1 167.1 544.3 272 544.3z"
                />
                <path
                  fill="#fbbc04"
                  d="M119.4 325.1c-10.5-31.3-10.5-65.3 0-96.6v-70.2H30.2C-10.1 206.6-10.1 339.7 30.2 412l89.2-86.9z"
                />
                <path
                  fill="#ea4335"
                  d="M272 107.7c39.6-.6 77.7 13.6 107 40.1l79.9-79.9C406.3 24 344.9 0 272 0 167.1 0 74.3 62.2 30.2 159.7l89.2 70.2C140.9 155.6 201 107.7 272 107.7z"
                />
              </svg>
              Continue with Google
            </button>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section className="min-h-screen bg-slate-950 text-slate-100">
      <div className="mx-auto flex w-full max-w-4xl flex-col gap-6 px-6 py-10">
        <header className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-3xl font-semibold">My Todos</h1>
            <p className="text-sm text-slate-400">
              Signed in as <span className="font-medium text-slate-200">{user.email ?? user.uid}</span>
            </p>
          </div>
          <button
            type="button"
            onClick={handleSignOut}
            className="self-start rounded-md border border-slate-700 px-3 py-2 text-sm font-semibold text-slate-200 transition hover:border-rose-500/70 hover:text-rose-200"
          >
            Log out
          </button>
        </header>

        <nav className="flex flex-wrap gap-2">
          {FILTERS.map((filterKey) => (
            <button
              key={filterKey}
              type="button"
              onClick={() => setFilter(filterKey)}
              className={`rounded-full px-4 py-2 text-sm font-semibold transition ${
                filter === filterKey
                  ? 'bg-emerald-500 text-slate-900 shadow'
                  : 'border border-slate-700 text-slate-300 hover:border-slate-500 hover:text-white'
              }`}
            >
              {filterKey === 'all' ? 'All' : filterKey === 'active' ? 'Active' : 'Completed'}
            </button>
          ))}
        </nav>

        <form
          onSubmit={handleCreateTodo}
          className="flex flex-col gap-3 rounded-2xl border border-slate-800 bg-slate-900/60 p-6 shadow"
        >
          <h2 className="text-lg font-semibold text-slate-200">Add a new todo</h2>
          <div className="flex flex-col gap-2 sm:flex-row">
            <input
              value={newTitle}
              onChange={(event) => setNewTitle(event.target.value)}
              type="text"
              placeholder="What needs to be done?"
              className="flex-1 rounded-md border border-slate-700 bg-slate-950 px-3 py-2 text-base text-slate-100 shadow focus:border-emerald-400 focus:outline-none focus:ring-2 focus:ring-emerald-400/30"
              maxLength={100}
              required
            />
            <input
              value={newDueDate}
              onChange={(event) => setNewDueDate(event.target.value)}
              type="date"
              className="w-full rounded-md border border-slate-700 bg-slate-950 px-3 py-2 text-base text-slate-100 shadow focus:border-emerald-400 focus:outline-none focus:ring-2 focus:ring-emerald-400/30 sm:w-48"
            />
            <button
              type="submit"
              className="rounded-md bg-emerald-500 px-4 py-2 text-sm font-semibold text-slate-900 shadow transition hover:bg-emerald-400 disabled:cursor-not-allowed disabled:opacity-60"
              disabled={createMutation.isPending}
            >
              {createMutation.isPending ? 'Adding…' : 'Add todo'}
            </button>
          </div>
          {formError ? (
            <p className="text-sm text-rose-300">{formError}</p>
          ) : null}
        </form>

        <section className="rounded-2xl border border-slate-800 bg-slate-900/60 shadow">
          {listError ? (
            <div className="p-8 text-center text-rose-300">{listError}</div>
          ) : todosLoading ? (
            <div className="p-8 text-center text-slate-400">Loading your todos…</div>
          ) : sortedTodos.length === 0 ? (
            <div className="flex flex-col items-center gap-3 p-10 text-center text-slate-400">
              <span className="text-2xl">✨</span>
              <p className="text-base">You have no todos in this view. Time to add something!</p>
            </div>
          ) : (
            <ul className="divide-y divide-slate-800">
              {sortedTodos.map((todo) => (
                <li key={todo.id} className="flex flex-col gap-3 p-5 sm:flex-row sm:items-start sm:justify-between">
                  <div className="flex flex-1 items-start gap-3">
                    <input
                      type="checkbox"
                      className="mt-1 h-5 w-5 rounded border-slate-600 bg-slate-900 text-emerald-500 focus:ring-emerald-400"
                      checked={todo.done}
                      onChange={() => handleToggle(todo)}
                      disabled={toggleMutation.isPending}
                      aria-label={`Mark todo '${todo.title}' as ${todo.done ? 'not done' : 'done'}`}
                    />
                    <div className="flex flex-1 flex-col gap-2">
                      {editingId === todo.id ? (
                        <form onSubmit={submitEditing} className="flex flex-col gap-2 sm:flex-row sm:items-center">
                          <input
                            value={editTitle}
                            onChange={(event) => setEditTitle(event.target.value)}
                            className="flex-1 rounded-md border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-slate-100 focus:border-emerald-400 focus:outline-none focus:ring-2 focus:ring-emerald-400/30"
                            maxLength={100}
                            required
                          />
                          <input
                            value={editDueDate}
                            onChange={(event) => setEditDueDate(event.target.value)}
                            type="date"
                            className="rounded-md border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-slate-100 focus:border-emerald-400 focus:outline-none focus:ring-2 focus:ring-emerald-400/30"
                          />
                          <div className="flex gap-2">
                            <button
                              type="submit"
                              className="rounded-md bg-emerald-500 px-3 py-2 text-sm font-semibold text-slate-900 shadow hover:bg-emerald-400"
                              disabled={updateMutation.isPending}
                            >
                              Save
                            </button>
                            <button
                              type="button"
                              onClick={cancelEditing}
                              className="rounded-md border border-slate-700 px-3 py-2 text-sm font-semibold text-slate-200 transition hover:border-slate-500"
                            >
                              Cancel
                            </button>
                          </div>
                        </form>
                      ) : (
                        <div className="flex flex-col gap-1">
                          <p
                            className={`text-base font-medium ${
                              todo.done ? 'text-slate-400 line-through' : 'text-slate-100'
                            }`}
                          >
                            {todo.title}
                          </p>
                          <p className="text-xs text-slate-500">Due: {formatDate(todo.dueDate)}</p>
                          <p className="text-xs text-slate-500">
                            Updated {new Date(todo.updatedAt).toLocaleString()}
                          </p>
                        </div>
                      )}
                    </div>
                  </div>
                  {editingId !== todo.id ? (
                    <div className="flex items-center gap-2">
                      <button
                        type="button"
                        onClick={() => startEditing(todo)}
                        className="rounded-md border border-slate-700 px-3 py-2 text-xs font-semibold text-slate-200 transition hover:border-slate-500"
                      >
                        Edit
                      </button>
                      <button
                        type="button"
                        onClick={() => handleDelete(todo.id)}
                        className="rounded-md border border-rose-500/70 px-3 py-2 text-xs font-semibold text-rose-200 transition hover:bg-rose-500/10"
                        disabled={deleteMutation.isPending}
                      >
                        Delete
                      </button>
                    </div>
                  ) : null}
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </section>
  );
}
