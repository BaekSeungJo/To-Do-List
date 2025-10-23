'use client';

import Link from 'next/link';
import { useEffect } from 'react';

export default function HomePage() {
  useEffect(() => {
    console.info('Todo web scaffold ready');
  }, []);

  return (
    <main className="min-h-screen bg-slate-950 text-slate-100 flex flex-col items-center justify-center gap-6 p-8">
      <h1 className="text-4xl font-bold">Firebase Todo Service</h1>
      <p className="text-lg text-slate-300 max-w-xl text-center">
        Bootstrap for a Firebase-authenticated to-do manager built with Next.js 14, React Query,
        and a Spring Boot backend.
      </p>
      <Link
        href="/todos"
        className="rounded bg-emerald-500 px-4 py-2 font-semibold text-slate-950 shadow hover:bg-emerald-400"
      >
        Enter App
      </Link>
    </main>
  );
}
