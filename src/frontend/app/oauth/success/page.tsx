'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '../../../lib/auth-context';

export default function OAuthSuccessPage() {
  const router = useRouter();
  const { refresh, user, loading } = useAuth();

  useEffect(() => {
    // Refresh auth context and redirect to home
    refresh().then(() => {
      router.replace('/');
    });
  }, [refresh, router]);

  return (
    <div className="flex flex-col items-center justify-center h-full dot-bg">
      <div className="card p-8 text-center">
        <h1 className="text-2xl mb-4" style={{ fontFamily: "'Press Start 2P', monospace" }}>
          vibes
        </h1>
        <p className="text-text-muted mb-4">
          {loading ? 'Completing sign in...' : user ? 'Redirecting...' : 'Sign in successful!'}
        </p>
        <div className="animate-pulse">
          <div className="w-8 h-8 border-4 border-accent border-t-transparent rounded-full animate-spin mx-auto" />
        </div>
      </div>
    </div>
  );
}
