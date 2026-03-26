'use client';

import { useEffect, useRef, useState } from 'react';
import { searchUsers, createGroup } from '../lib/api';
import type { UserProfile, GroupChatResponse } from '../lib/api';
import { useRouter } from 'next/navigation';

interface CreateGroupModalProps {
  onClose: () => void;
  onGroupCreated: (group: GroupChatResponse) => void;
}

export default function CreateGroupModal({ onClose, onGroupCreated }: CreateGroupModalProps) {
  const [groupName, setGroupName] = useState('');
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<UserProfile[]>([]);
  const [selectedMembers, setSelectedMembers] = useState<UserProfile[]>([]);
  const [searching, setSearching] = useState(false);
  const [creating, setCreating] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);
  const nameInputRef = useRef<HTMLInputElement>(null);
  const router = useRouter();

  // Auto-focus
  useEffect(() => { nameInputRef.current?.focus(); }, []);

  // Debounced search
  useEffect(() => {
    if (!query.trim()) { setResults([]); return; }
    const t = setTimeout(async () => {
      setSearching(true);
      try {
        const r = await searchUsers(query.trim());
        // Filter out already selected members
        const filtered = r.filter(u => !selectedMembers.some(m => m.id === u.id));
        setResults(filtered);
      } catch {
        setResults([]);
      } finally {
        setSearching(false);
      }
    }, 300);
    return () => clearTimeout(t);
  }, [query, selectedMembers]);

  function toggleMember(user: UserProfile) {
    setSelectedMembers(prev => {
      const exists = prev.some(m => m.id === user.id);
      if (exists) {
        return prev.filter(m => m.id !== user.id);
      } else {
        return [...prev, user];
      }
    });
    setQuery('');
    setResults([]);
    inputRef.current?.focus();
  }

  async function handleCreate() {
    if (!groupName.trim() || selectedMembers.length === 0) return;
    
    setCreating(true);
    try {
      const group = await createGroup({
        name: groupName.trim(),
        memberIds: selectedMembers.map(m => m.id),
      });
      onGroupCreated(group);
      router.push(`/chat/${group.chatId}`);
      onClose();
    } catch (err) {
      console.error('Failed to create group', err);
      alert('Failed to create group. Please try again.');
    } finally {
      setCreating(false);
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
        style={{ width: '100%', maxWidth: 480, padding: 0, overflow: 'hidden' }}
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
            New Group
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

        {/* Group name input */}
        <div style={{ padding: '14px 20px', borderBottom: '2px solid var(--border)' }}>
          <input
            ref={nameInputRef}
            type="text"
            value={groupName}
            onChange={(e) => setGroupName(e.target.value)}
            placeholder="Group name..."
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

        {/* Selected members */}
        {selectedMembers.length > 0 && (
          <div style={{ 
            padding: '10px 20px', 
            borderBottom: '2px solid var(--border)',
            display: 'flex',
            flexWrap: 'wrap',
            gap: 8,
            maxHeight: 100,
            overflowY: 'auto',
          }}>
            {selectedMembers.map((member) => (
              <div
                key={member.id}
                onClick={() => toggleMember(member)}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 6,
                  padding: '4px 10px',
                  background: 'var(--yellow-800)',
                  borderRadius: 'var(--radius-sm)',
                  cursor: 'pointer',
                  border: '2px solid var(--border)',
                }}
              >
                <span style={{ fontSize: 13, fontWeight: 700, color: 'var(--text)' }}>
                  {member.username}
                </span>
                <span style={{ fontSize: 14, color: 'var(--text-muted)' }}>×</span>
              </div>
            ))}
          </div>
        )}

        {/* Member search */}
        <div style={{ padding: '14px 20px', borderBottom: '2px solid var(--border)' }}>
          <input
            ref={inputRef}
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search users to add..."
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
        <div style={{ maxHeight: 240, overflowY: 'auto' }}>
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
              onClick={() => toggleMember(u)}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: 12,
                padding: '12px 20px',
                borderBottom: '1px solid var(--border)',
                cursor: 'pointer',
                background: 'transparent',
                transition: 'background 0.12s',
              }}
              onMouseEnter={(e) => e.currentTarget.style.background = 'var(--bg-hover)'}
              onMouseLeave={(e) => e.currentTarget.style.background = 'transparent'}
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
              <span style={{ fontSize: 20, color: 'var(--accent)' }}>+</span>
            </div>
          ))}
        </div>

        {/* Footer */}
        <div style={{ 
          padding: '14px 20px', 
          borderTop: '2px solid var(--border)',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
        }}>
          <span style={{ fontSize: 13, color: 'var(--text-muted)' }}>
            {selectedMembers.length} member{selectedMembers.length !== 1 ? 's' : ''} selected
          </span>
          <button
            onClick={handleCreate}
            disabled={creating || !groupName.trim() || selectedMembers.length === 0}
            style={{
              padding: '10px 24px',
              background: creating || !groupName.trim() || selectedMembers.length === 0 
                ? 'var(--yellow-700)' 
                : 'var(--accent)',
              color: creating || !groupName.trim() || selectedMembers.length === 0 
                ? 'var(--text-muted)' 
                : '#0d0d0d',
              border: 'none',
              borderRadius: 'var(--radius-sm)',
              fontFamily: 'Press Start 2P, monospace',
              fontSize: 14,
              cursor: creating || !groupName.trim() || selectedMembers.length === 0 
                ? 'not-allowed' 
                : 'pointer',
              boxShadow: creating || !groupName.trim() || selectedMembers.length === 0 
                ? 'none' 
                : 'var(--shadow)',
            }}
          >
            {creating ? 'Creating…' : 'Create Group'}
          </button>
        </div>
      </div>
    </div>
  );
}
