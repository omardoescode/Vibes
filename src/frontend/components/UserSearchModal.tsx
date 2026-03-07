'use client';

import { useEffect, useRef, useState } from 'react';
import { searchUsers, startChat } from '../lib/api';
import type { UserProfile, ChatSummary } from '../lib/api';
import { useRouter } from 'next/navigation';

interface UserSearchModalProps {
  onClose: () => void;
  onChatStarted: (chat: ChatSummary) => void;
}

export default function UserSearchModal({ onClose, onChatStarted }: UserSearchModalProps) {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<UserProfile[]>([]);
  const [searching, setSearching] = useState(false);
  const [starting, setStarting] = useState<string | null>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const router = useRouter();

  // Auto-focus
  useEffect(() => { inputRef.current?.focus(); }, []);

  // Debounced search
  useEffect(() => {
    if (!query.trim()) { setResults([]); return; }
    const t = setTimeout(async () => {
      setSearching(true);
      try {
        const r = await searchUsers(query.trim());
        setResults(r);
      } catch {
        setResults([]);
      } finally {
        setSearching(false);
      }
    }, 300);
    return () => clearTimeout(t);
  }, [query]);

  async function handleStart(userId: string) {
    setStarting(userId);
    try {
      const chat = await startChat(userId);
      onChatStarted(chat);
      router.push(`/chat/${chat.chatId}`);
      onClose();
    } catch (err) {
      console.error('Failed to start chat', err);
    } finally {
      setStarting(null);
    }
  }

  return (
    /* Backdrop */
    <div
      onClick={onClose}
      style={{
        position: 'fixed',
        inset: 0,
        background: 'rgba(10,8,0,0.75)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 1000,
        padding: 24,
      }}
    >
      {/* Modal */}
      <div
        onClick={(e) => e.stopPropagation()}
        className="card animate-bounce-in"
        style={{ width: '100%', maxWidth: 420, padding: 0, overflow: 'hidden' }}
      >
        {/* Header */}
        <div
          style={{
            padding: '18px 20px 14px',
            borderBottom: '2px solid var(--border)',
            display: 'flex',
            alignItems: 'center',
            gap: 12,
          }}
        >
          <h2 style={{ flex: 1, margin: 0, fontSize: 22, color: 'var(--text)' }}>
            New Chat
          </h2>
          <button
            onClick={onClose}
            style={{
              background: 'none',
              border: 'none',
              fontSize: 22,
              cursor: 'pointer',
              color: 'var(--text-muted)',
              lineHeight: 1,
            }}
          >
            ✕
          </button>
        </div>

        {/* Search input */}
        <div style={{ padding: '14px 20px', borderBottom: '2px solid var(--border)' }}>
          <input
            ref={inputRef}
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search by username…"
            style={{
              width: '100%',
              padding: '10px 14px',
              border: '2px solid var(--border)',
              borderRadius: 'var(--radius-sm)',
              background: 'var(--bg)',
              color: 'var(--text)',
              fontSize: 15,
              fontFamily: 'Rajdhani, sans-serif',
              fontWeight: 600,
              outline: 'none',
              boxSizing: 'border-box',
            }}
          />
        </div>

        {/* Results */}
        <div style={{ maxHeight: 320, overflowY: 'auto' }}>
          {searching && (
            <div style={{ padding: '20px', textAlign: 'center', color: 'var(--text-muted)' }}>
              Searching…
            </div>
          )}
          {!searching && query.trim() && results.length === 0 && (
            <div style={{ padding: '20px', textAlign: 'center', color: 'var(--text-muted)', fontWeight: 600, fontSize: 14 }}>
              No users found for &quot;{query}&quot;
            </div>
          )}
          {results.map((u) => (
            <div
              key={u.id}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: 12,
                padding: '12px 20px',
                borderBottom: '1px solid var(--border)',
              }}
            >
              <div
                style={{
                  width: 38,
                  height: 38,
                  borderRadius: '50%',
                  background: 'var(--yellow-800)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontFamily: 'Rajdhani, sans-serif',
                  fontSize: 18,
                  color: 'var(--accent)',
                  border: '2px solid var(--border)',
                  flexShrink: 0,
                }}
              >
                {u.username[0]?.toUpperCase()}
              </div>
              <span style={{ flex: 1, fontWeight: 700, fontSize: 14, color: 'var(--text)' }}>
                {u.username}
              </span>
              <button
                onClick={() => handleStart(u.id)}
                disabled={starting === u.id}
                style={{
                  padding: '7px 16px',
                  background: starting === u.id ? 'var(--yellow-700)' : 'var(--accent)',
                  color: starting === u.id ? 'var(--text-muted)' : '#0d0d0d',
                  border: 'none',
                  borderRadius: 'var(--radius-sm)',
                  fontFamily: 'Press Start 2P, monospace',
                  fontSize: 14,
                  cursor: starting === u.id ? 'not-allowed' : 'pointer',
                  boxShadow: starting === u.id ? 'none' : 'var(--shadow)',
                }}
              >
                {starting === u.id ? '…' : 'Chat'}
              </button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
