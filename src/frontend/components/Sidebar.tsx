'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useRef, useState } from 'react';
import { Search, Users, Plus, LogOut } from 'lucide-react';
import { useAuth } from '../lib/auth-context';
import { uploadAvatar, toProxyUrl } from '../lib/api';
import type { ChatSummary } from '../lib/api';
import SearchModal from './SearchModal';

interface SidebarProps {
  chats: ChatSummary[];
  unreadCounts: Record<string, number>;
  onNewChat: () => void;
  onNewGroup: () => void;
  onChatClick?: (chatId: string) => void;
}

export default function Sidebar({ chats, unreadCounts, onNewChat, onNewGroup, onChatClick }: SidebarProps) {
  const { user, logout, refresh } = useAuth();
  const pathname = usePathname();
  const router = useRouter();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [uploading, setUploading] = useState(false);
  const [isSearchModalOpen, setIsSearchModalOpen] = useState(false);
  
  // Get current chat ID from pathname
  const currentChatId = pathname?.startsWith('/chat/') 
    ? pathname.split('/chat/')[1] 
    : undefined;

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
      await refresh();
    } catch (err) {
      console.error('Avatar upload failed', err);
    } finally {
      setUploading(false);
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
          padding: '16px 16px 12px',
          borderBottom: '2px solid var(--border)',
        }}
      >
        {/* Title Row */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            marginBottom: 12,
          }}
        >
          <h1
            style={{
              fontSize: 28,
              color: 'var(--accent)',
              margin: 0,
              lineHeight: 1,
              fontWeight: 700,
              letterSpacing: '-0.5px',
            }}
          >
            vibes
          </h1>
        </div>

        {/* Action Toolbar */}
        <div
          style={{
            display: 'flex',
            gap: 6,
            background: 'var(--bg)',
            padding: '6px',
            borderRadius: 'var(--radius-md)',
            border: '2px solid var(--border)',
            justifyContent: 'space-between',
          }}
        >
          <div style={{ display: 'flex', gap: 6 }}>
            <ToolbarButton
              onClick={() => setIsSearchModalOpen(true)}
              icon={<Search size={18} />}
              label="Search"
              variant="secondary"
              showLabel={false}
            />
            <ToolbarButton
              onClick={onNewGroup}
              icon={<Users size={18} />}
              label="Group"
              variant="secondary"
              showLabel={false}
            />
          </div>
          <ToolbarButton
            onClick={onNewChat}
            icon={<Plus size={16} />}
            label="New"
            variant="primary"
            showLabel={true}
          />
        </div>
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
        {/* Avatar */}
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
            padding: 4,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <LogOut size={20} />
        </button>
      </div>

      <SearchModal
        chatId={currentChatId}
        isOpen={isSearchModalOpen}
        onClose={() => setIsSearchModalOpen(false)}
      />
    </aside>
  );
}

// Toolbar Button Component
interface ToolbarButtonProps {
  onClick: () => void;
  icon: React.ReactNode;
  label: string;
  variant: 'primary' | 'secondary';
  showLabel?: boolean;
}

function ToolbarButton({ onClick, icon, label, variant, showLabel = true }: ToolbarButtonProps) {
  const [isHovered, setIsHovered] = useState(false);

  const isPrimary = variant === 'primary';

  return (
    <button
      onClick={onClick}
      title={label}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: showLabel ? 6 : 0,
        padding: isPrimary ? (showLabel ? '8px 12px' : '8px') : '8px',
        width: showLabel ? 'auto' : '36px',
        height: '36px',
        borderRadius: 'var(--radius-sm)',
        border: isPrimary ? 'none' : '2px solid transparent',
        background: isPrimary
          ? 'var(--accent)'
          : isHovered
          ? 'var(--yellow-800)'
          : 'transparent',
        color: isPrimary ? '#0d0d0d' : 'var(--text-muted)',
        fontSize: isPrimary ? 14 : 14,
        fontWeight: 700,
        cursor: 'pointer',
        transition: 'all 0.15s ease',
        whiteSpace: 'nowrap',
        flexShrink: 0,
      }}
    >
      {icon}
      {showLabel && (
        <span
          style={{
            fontSize: 12,
            fontWeight: 600,
          }}
        >
          {label}
        </span>
      )}
    </button>
  );
}

// ChatListItem Component
function ChatListItem({
  chat,
  unreadCount,
  active,
  onClick,
}: {
  chat: ChatSummary;
  unreadCount: number;
  active: boolean;
  onClick: () => void;
}) {
  const isGroup = chat.type === 'GROUP';
  const displayName = isGroup ? chat.name : chat.otherUsername;
  const initial = displayName?.[0]?.toUpperCase() ?? '?';
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
      {isGroup ? (
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
            color: 'var(--accent)',
            flexShrink: 0,
          }}
        >
          <Users size={20} />
        </div>
      ) : chat.otherUserProfilePicture ? (
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
          {displayName}
        </div>
        <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 2 }}>
          {isGroup && chat.memberCount ? `${chat.memberCount} members` : formatDate(chat.createdAt)}
        </div>
      </div>

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