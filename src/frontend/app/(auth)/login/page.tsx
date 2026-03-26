'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { login } from '../../../lib/api';
import { useAuth } from '../../../lib/auth-context';

export default function LoginPage() {
  const router = useRouter();
  const { refresh, user, loading: authLoading } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // Already authenticated — redirect immediately
  useEffect(() => {
    if (!authLoading && user) {
      router.replace('/');
    }
  }, [authLoading, user, router]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(email, password);
      // Populate auth context before navigating — chat layout needs user != null
      await refresh();
      router.push('/');
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Login failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div
      className="card animate-bounce-in w-full max-w-[420px] p-10"
    >
      {/* Logo */}
      <div className="text-center mb-7">
        <h1 className="text-5xl text-accent m-0 leading-none">
          vibes
        </h1>
        <p className="text-text-muted mt-1.5 text-sm font-semibold">
          good vibes only ✨
        </p>
      </div>

      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <div>
          <label className="block text-xs font-bold text-text-muted uppercase tracking-wider mb-1.5">
            Email
          </label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="you@example.com"
            required
            className="w-full px-3.5 py-2.5 border-2 border-border rounded-sm bg-bg text-text text-base font-semibold outline-none transition-colors focus:border-accent"
            style={{ fontFamily: 'Rajdhani, sans-serif' }}
          />
        </div>

        <div>
          <label className="block text-xs font-bold text-text-muted uppercase tracking-wider mb-1.5">
            Password
          </label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="••••••••"
            required
            className="w-full px-3.5 py-2.5 border-2 border-border rounded-sm bg-bg text-text text-base font-semibold outline-none transition-colors focus:border-accent"
            style={{ fontFamily: 'Rajdhani, sans-serif' }}
          />
        </div>

        {error && (
          <div className="bg-[#fff3f0] border-[1.5px] border-pop rounded-sm p-2.5 text-pop text-sm font-semibold">
            {error}
          </div>
        )}

        <button 
          type="submit" 
          disabled={loading} 
          className="mt-1 py-3 px-4 rounded-sm text-base transition-all hover:-translate-y-0.5 disabled:translate-y-0 disabled:shadow-none"
          style={{
            background: loading ? 'var(--yellow-800)' : 'var(--accent)',
            color: '#0d0d0d',
            fontFamily: "'Press Start 2P', monospace",
            boxShadow: loading ? 'none' : 'var(--shadow-md)',
            cursor: loading ? 'not-allowed' : 'pointer',
            letterSpacing: '0.02em'
          }}
        >
          {loading ? 'Signing in…' : 'Sign in'}
        </button>
      </form>

      <p className="text-center mt-6 text-sm text-text-muted">
        No account?{' '}
        <Link href="/register" className="text-accent font-bold no-underline hover:underline">
          Register
        </Link>
      </p>

      <div className="text-center mt-5">
        <div className="flex items-center gap-3 mb-4 text-text-muted text-sm">
          <div className="flex-1 h-px bg-border" />
          <span>OR</span>
          <div className="flex-1 h-px bg-border" />
        </div>
        
        <a 
          href={`${process.env.NEXT_PUBLIC_API_URL}/auth/oauth/github/authorize`}
          className="inline-flex items-center justify-center px-6 py-3 rounded-sm font-semibold transition-all hover:-translate-y-0.5"
          style={{
            background: '#24292e',
            color: '#ffffff',
            fontFamily: 'Rajdhani, sans-serif',
            boxShadow: 'var(--shadow-md)',
            fontSize: '16px'
          }}
        >
          <svg 
            viewBox="0 0 24 24" 
            width="20" 
            height="20" 
            className="mr-2.5"
            fill="currentColor"
          >
            <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
          </svg>
          Continue with GitHub
        </a>
      </div>
    </div>
  );
}
