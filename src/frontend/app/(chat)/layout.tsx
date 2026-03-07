'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '../../lib/auth-context';
import { ChatSocketProvider } from '../../lib/chat-socket-context';
import { listChats } from '../../lib/api';
import type { ChatSummary, Message } from '../../lib/api';
import Sidebar from '../../components/Sidebar';
import UserSearchModal from '../../components/UserSearchModal';

export default function ChatLayout({ children }: { children: React.ReactNode }) {
  const { user, loading } = useAuth();
  const router = useRouter();
  const [chats, setChats] = useState<ChatSummary[]>([]);
  const [showSearch, setShowSearch] = useState(false);

  // Redirect to login if not authenticated
  useEffect(() => {
    if (!loading && !user) {
      router.replace('/login');
    }
  }, [loading, user, router]);

  // Load chat list
  useEffect(() => {
    if (!user) return;
    listChats().then(setChats).catch(console.error);
  }, [user]);

  function handleChatStarted(chat: ChatSummary) {
    setChats((prev) => {
      if (prev.some((c) => c.chatId === chat.chatId)) return prev;
      return [chat, ...prev];
    });
  }

  // Refresh sidebar when any new message arrives (handled inside SidebarRefresher)
  function handleAnyMessage(_msg: Message) {
    listChats().then(setChats).catch(() => {});
  }

  if (loading) {
    return (
      <div
        className="dot-bg"
        style={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: 32,
          color: 'var(--accent)',
          fontFamily: 'Rajdhani, sans-serif',
        }}
      >
        vibes…
      </div>
    );
  }

  if (!user) return null;

  return (
    <ChatSocketProvider userId={user.id}>
      <SidebarRefresher onMessage={handleAnyMessage} />
      <div style={{ display: 'flex', height: '100vh', overflow: 'hidden' }}>
        <Sidebar chats={chats} onNewChat={() => setShowSearch(true)} />
        <main style={{ flex: 1, overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
          {children}
        </main>
        {showSearch && (
          <UserSearchModal
            onClose={() => setShowSearch(false)}
            onChatStarted={handleChatStarted}
          />
        )}
      </div>
    </ChatSocketProvider>
  );
}

// ---------------------------------------------------------------------------
// SidebarRefresher — subscribes to incoming messages inside the provider context.
// Re-fetches the chat list whenever any message arrives.
// ---------------------------------------------------------------------------

import { useEffect as useEff } from 'react';
import { useChatSocket } from '../../lib/chat-socket-context';

function SidebarRefresher({ onMessage }: { onMessage: (msg: Message) => void }) {
  const { subscribe } = useChatSocket();

  useEff(() => {
    return subscribe(onMessage);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [subscribe]);

  return null;
}

