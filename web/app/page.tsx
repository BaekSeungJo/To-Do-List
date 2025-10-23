import Link from 'next/link';

export default function Home() {
  return (
    <main style={{ display: 'grid', placeItems: 'center', minHeight: '100vh', padding: '2rem' }}>
      <div style={{ maxWidth: 480, textAlign: 'center' }}>
        <h1>Firebase 기반 To-Do</h1>
        <p>로그인 후 개인 할 일을 안전하게 관리하세요.</p>
        <Link href="/todos" style={{ display: 'inline-block', marginTop: '1.5rem', padding: '0.75rem 1.5rem', background: '#2563eb', color: '#fff', borderRadius: '0.5rem' }}>
          To-Do 시작하기
        </Link>
      </div>
    </main>
  );
}
