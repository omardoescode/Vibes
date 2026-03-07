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
      className="card animate-bounce-in"
      style={{ width: '100%', maxWidth: 420, padding: '40px 36px' }}
    >
      {/* Logo */}
      <div style={{ textAlign: 'center', marginBottom: 28 }}>
        <h1 style={{ fontSize: 48, color: 'var(--accent)', margin: 0, lineHeight: 1 }}>
          vibes
        </h1>
        <p style={{ color: 'var(--text-muted)', marginTop: 6, fontSize: 14, fontWeight: 600 }}>
          good vibes only ✨
        </p>
      </div>

      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
        <div>
          <label style={labelStyle}>Email</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="you@example.com"
            required
            style={inputStyle}
          />
        </div>

        <div>
          <label style={labelStyle}>Password</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="••••••••"
            required
            style={inputStyle}
          />
        </div>

        {error && (
          <div
            style={{
        background: '#fff3f0',
        border: '1.5px solid #c0390a',
              borderRadius: 'var(--radius-sm)',
              padding: '10px 14px',
              color: 'var(--pop)',
              fontSize: 13,
              fontWeight: 600,
            }}
          >
            {error}
          </div>
        )}

        <button type="submit" disabled={loading} style={btnStyle(loading)}>
          {loading ? 'Signing in…' : 'Sign in'}
        </button>
      </form>

      <p style={{ textAlign: 'center', marginTop: 24, fontSize: 14, color: 'var(--text-muted)' }}>
        No account?{' '}
        <Link href="/register" style={{ color: 'var(--accent)', fontWeight: 700, textDecoration: 'none' }}>
          Register
        </Link>
      </p>
    </div>
  );
}

const labelStyle: React.CSSProperties = {
  display: 'block',
  fontSize: 13,
  fontWeight: 700,
  color: 'var(--text-muted)',
  marginBottom: 6,
  textTransform: 'uppercase',
  letterSpacing: '0.05em',
};

const inputStyle: React.CSSProperties = {
  width: '100%',
  padding: '11px 14px',
  border: '2px solid var(--border)',
  borderRadius: 'var(--radius-sm)',
  background: 'var(--bg)',
  color: 'var(--text)',
  fontSize: 15,
  fontFamily: 'Rajdhani, sans-serif',
  fontWeight: 600,
  outline: 'none',
  transition: 'border-color 0.15s',
  boxSizing: 'border-box',
};

const btnStyle = (disabled: boolean): React.CSSProperties => ({
  marginTop: 4,
  padding: '13px',
  background: disabled ? 'var(--yellow-800)' : 'var(--accent)',
  color: '#0d0d0d',
  border: 'none',
  borderRadius: 'var(--radius-sm)',
  fontSize: 16,
  fontFamily: 'Press Start 2P, monospace',
  fontWeight: 400,
  cursor: disabled ? 'not-allowed' : 'pointer',
  boxShadow: disabled ? 'none' : 'var(--shadow-md)',
  transition: 'transform 0.1s, box-shadow 0.1s',
  letterSpacing: '0.02em',
});
