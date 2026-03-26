'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useRef, useState } from 'react';
import { useAuth } from '../lib/auth-context';
import { uploadAvatar, toProxyUrl } from '../lib/api';
import type { ChatSummary } from '../lib/api';

interface SidebarProps {
  chats: ChatSummary[];
  unreadCounts: Record<string, number>;
  onNewChat: () => void;
  onChatClick?: (chatId: string) => void;
}

export default function Sidebar({ chats, unreadCounts, onNewChat, onChatClick }: SidebarProps) {
  const { user, logout, refresh } = useAuth();
  const pathname = usePathname();
  const router = useRouter();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [uploading, setUploading] = useState(false);

  async function handleLogout() {
    await logout();
    router.push('/login');
  }

  async function handleAvatarChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploading(true);
    try {
      await uploadAvatar(file);
      // Re-fetch /auth/me so the context (and this component) reflects the new URL
      await refresh();
    } catch (err) {
      console.error('Avatar upload failed', err);
    } finally {
      setUploading(false);
      // Reset so the same file can be re-selected
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  }

  const handleChatClick = (chatId: string) => {
    if (onChatClick) {
      onChatClick(chatId);
    }
  };

  return (
    <aside
      style={{
        width: 300,
        minWidth: 300,
        height: '100vh',
        background: 'var(--bg-card)',
        borderRight: '2px solid var(--border)',
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden',
      }}
    >
      {/* Header */}
      <div
        style={{
          padding: '20px 20px 16px',
          borderBottom: '2px solid var(--border)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: 12,
        }}
      >
        <h1 style={{ fontSize: 32, color: 'var(--accent)', margin: 0, lineHeight: 1 }}>
          vibes
        </h1>
        <button
          onClick={onNewChat}
          title="New chat"
          style={{
            width: 38,
            height: 38,
            borderRadius: '50%',
            background: 'var(--accent)',
            border: 'none',
            color: '#1a1400',
            fontSize: 22,
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: 'var(--shadow)',
            flexShrink: 0,
          }}
        >
          +
        </button>
      </div>

      {/* Chat list */}
      <div style={{ flex: 1, overflowY: 'auto', padding: '8px 0' }}>
        {chats.length === 0 ? (
          <div
            style={{
              padding: '32px 20px',
              textAlign: 'center',
              color: 'var(--text-muted)',
              fontSize: 14,
              fontWeight: 600,
            }}
          >
            No chats yet. Hit + to start one!
          </div>
        ) : (
          chats.map((chat) => (
            <ChatListItem
              key={chat.chatId}
              chat={chat}
              unreadCount={unreadCounts[chat.chatId] || 0}
              active={pathname === `/chat/${chat.chatId}`}
              onClick={() => handleChatClick(chat.chatId)}
            />
          ))
        )}
      </div>

      {/* User footer */}
      <div
        style={{
          borderTop: '2px solid var(--border)',
          padding: '14px 16px',
          display: 'flex',
          alignItems: 'center',
          gap: 12,
        }}
      >
        {/* Avatar — click to upload */}
        <button
          onClick={() => fileInputRef.current?.click()}
          title="Change profile picture"
          disabled={uploading}
          style={{
            width: 36,
            height: 36,
            borderRadius: '50%',
            background: 'var(--yellow-700)',
            border: '2px solid var(--border)',
            padding: 0,
            cursor: uploading ? 'not-allowed' : 'pointer',
            flexShrink: 0,
            overflow: 'hidden',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            position: 'relative',
            outline: 'none',
          }}
        >
          {uploading ? (
            <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>…</span>
          ) : user?.profilePictureUrl ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img
              src={toProxyUrl(user.profilePictureUrl)!}
              alt={user.username}
              style={{ width: '100%', height: '100%', objectFit: 'cover' }}
            />
          ) : (
            <span
              style={{
                fontFamily: 'Rajdhani, sans-serif',
                fontSize: 18,
                color: 'var(--accent-2)',
                lineHeight: 1,
              }}
            >
              {user?.username?.[0]?.toUpperCase() ?? '?'}
            </span>
          )}
        </button>

        {/* Hidden file input */}
        <input
          ref={fileInputRef}
          type="file"
          accept="image/*"
          style={{ display: 'none' }}
          onChange={handleAvatarChange}
        />

        <span
          style={{
            flex: 1,
            fontWeight: 700,
            fontSize: 14,
            color: 'var(--text)',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap',
          }}
        >
          {user?.username ?? '…'}
        </span>
        <button
          onClick={handleLogout}
          title="Sign out"
          style={{
            background: 'none',
            border: 'none',
            cursor: 'pointer',
            color: 'var(--text-muted)',
            fontSize: 18,
            padding: 4,
          }}
        >
          ↩
        </button>
      </div>
    </aside>
  );
}

// ---------------------------------------------------------------------------
// ChatListItem
// ---------------------------------------------------------------------------

function ChatListItem({ 
  chat, 
  unreadCount, 
  active, 
  onClick 
}: { 
  chat: ChatSummary; 
  unreadCount: number;
  active: boolean;
  onClick: () => void;
}) {
  const initial = chat.otherUsername?.[0]?.toUpperCase() ?? '?';
  const hasUnread = unreadCount > 0;

  return (
    <Link
      href={`/chat/${chat.chatId}`}
      onClick={onClick}
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: 12,
        padding: '11px 16px',
        textDecoration: 'none',
        background: active ? 'var(--bg-hover)' : 'transparent',
        borderLeft: active ? '3px solid var(--accent)' : '3px solid transparent',
        transition: 'background 0.12s',
      }}
    >
      {chat.otherUserProfilePicture ? (
        // eslint-disable-next-line @next/next/no-img-element
        <img
          src={toProxyUrl(chat.otherUserProfilePicture)!}
          alt={chat.otherUsername}
          style={{
            width: 40,
            height: 40,
            borderRadius: '50%',
            objectFit: 'cover',
            border: '2px solid var(--border)',
            flexShrink: 0,
          }}
        />
      ) : (
        <div
          style={{
            width: 40,
            height: 40,
            borderRadius: '50%',
            background: active ? 'var(--yellow-700)' : 'var(--yellow-900)',
            border: '2px solid var(--border)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontFamily: 'Rajdhani, sans-serif',
            fontSize: 18,
            color: 'var(--accent)',
            flexShrink: 0,
          }}
        >
          {initial}
        </div>
      )}

      <div style={{ flex: 1, overflow: 'hidden' }}>
        <div
          style={{
            fontWeight: hasUnread ? 800 : 700,
            fontSize: 14,
            color: 'var(--text)',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap',
          }}
        >
          {chat.otherUsername}
        </div>
        <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 2 }}>
          {formatDate(chat.createdAt)}
        </div>
      </div>

      {/* Unread badge */}
      {hasUnread && (
        <div
          style={{
            minWidth: 20,
            height: 20,
            borderRadius: '50%',
            background: 'var(--pop)',
            color: '#fff',
            fontSize: 11,
            fontWeight: 700,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexShrink: 0,
            padding: '0 6px',
          }}
        >
          {unreadCount > 99 ? '99+' : unreadCount}
        </div>
      )}
    </Link>
  );
}

function formatDate(iso: string): string {
  try {
    return new Date(iso).toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
  } catch {
    return '';
  }
}
