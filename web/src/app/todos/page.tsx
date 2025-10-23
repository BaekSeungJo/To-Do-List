export default function TodosPage() {
  return (
    <section className="min-h-screen bg-slate-900 text-slate-100 p-8">
      <div className="mx-auto flex max-w-3xl flex-col gap-4">
        <header>
          <h2 className="text-3xl font-semibold">My Todos</h2>
          <p className="text-slate-400">Todo workspace placeholder ready for Firebase authentication.</p>
        </header>
        <div className="rounded border border-slate-700 bg-slate-950 p-6 text-slate-300">
          <p>
            Implement todo list UI, filtering, and optimistic updates in the <code>UI-TODO</code> task.
          </p>
        </div>
      </div>
    </section>
  );
}
