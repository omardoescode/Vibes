'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import { useAuth } from '../../lib/auth-context';
import { ChatSocketProvider, useChatSocket, type NotificationContext } from '../../lib/chat-socket-context';
import { listChats, type ChatSummary, type Message } from '../../lib/api';
import Sidebar from '../../components/Sidebar';
import UserSearchModal from '../../components/UserSearchModal';

const BACKEND = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080';

export default function ChatLayout({ children }: { children: React.ReactNode }) {
  const { user, loading } = useAuth();
  const router = useRouter();

  // Redirect to login if not authenticated
  useEffect(() => {
    if (!loading && !user) {
      router.replace('/login');
    }
  }, [loading, user, router]);

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
      <ChatShell userId={user.id} userName={user.username}>
        {children}
      </ChatShell>
    </ChatSocketProvider>
  );
}

function ChatShell({ 
  userId, 
  userName, 
  children 
}: { 
  userId: string; 
  userName: string;
  children: React.ReactNode 
}) {
  const router = useRouter();
  const pathname = usePathname();
  const [chats, setChats] = useState<ChatSummary[]>([]);
  const [unreadCounts, setUnreadCounts] = useState<Record<string, number>>({});
  const [showSearch, setShowSearch] = useState(false);
  const { subscribe, subscribeToNotifications, notifyOpen } = useChatSocket();

  // Load initial unread counts
  useEffect(() => {
    fetch(`${BACKEND}/notifications/unread`, {
      credentials: 'include',
    })
      .then((res) => res.json())
      .then((counts: Record<string, number>) => {
        setUnreadCounts(counts);
      })
      .catch(console.error);
  }, []);

  // Load chat list
  useEffect(() => {
    listChats().then(setChats).catch(console.error);
  }, []);

  // Setup audio for notifications using Web Audio API
  const playNotificationSound = useCallback(() => {
    try {
      const audioContext = new (window.AudioContext || (window as any).webkitAudioContext)();
      const oscillator = audioContext.createOscillator();
      const gainNode = audioContext.createGain();
      
      oscillator.connect(gainNode);
      gainNode.connect(audioContext.destination);
      
      oscillator.frequency.value = 800; // Hz
      oscillator.type = 'sine';
      
      gainNode.gain.setValueAtTime(0.3, audioContext.currentTime);
      gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.5);
      
      oscillator.start(audioContext.currentTime);
      oscillator.stop(audioContext.currentTime + 0.5);
    } catch (err) {
      console.log('[audio] Web Audio API not supported:', err);
    }
  }, []);

  // Request notification permission early
  useEffect(() => {
    if (typeof Notification !== 'undefined' && Notification.permission === 'default') {
      Notification.requestPermission().then((permission) => {
        console.log('[notifications] permission:', permission);
      });
    }
  }, []);

  // Handle new messages
  const handleMessage = useCallback((msg: Message) => {
    // Refresh chat list
    listChats().then(setChats).catch(() => {});
  }, []);

  // Handle notifications
  const handleNotification = useCallback((notification: NotificationContext) => {
    console.log('[notification received]', notification.type, notification);
    
    switch (notification.type) {
      case 'NEW_MESSAGE':
        // Increment unread count for the chat
        setUnreadCounts((prev) => ({
          ...prev,
          [notification.chatId]: (prev[notification.chatId] || 0) + 1,
        }));

        // Play sound for all new messages
        playNotificationSound();

        // Show browser notification (even when tab is visible, but not if currently in that chat)
        const isVisible = document.visibilityState === 'visible';
        const currentChatMatch = pathname?.match(/^\/chat\/([^/]+)$/);
        const isInThisChat = currentChatMatch && currentChatMatch[1] === notification.chatId;
        
        console.log('[notification] tab visible:', isVisible, 'in this chat:', isInThisChat, 'permission:', Notification?.permission);
        
        // Show notification if:
        // 1. We have permission
        // 2. Either tab is not visible OR we're not currently viewing this specific chat
        if (typeof Notification !== 'undefined' && Notification.permission === 'granted') {
          if (!isVisible || !isInThisChat) {
            console.log('[notification] showing browser notification');
            new Notification(notification.title, {
              body: notification.body,
              icon: '/favicon.ico',
            });
          } else {
            console.log('[notification] not showing - currently viewing this chat');
          }
        } else {
          console.log('[notification] not showing - permission:', Notification?.permission);
        }
        break;

      case 'USER_ONLINE':
      case 'USER_OFFLINE':
        // Refresh chat list to show updated status
        listChats().then(setChats).catch(() => {});
        break;
    }
  }, [pathname]);

  // Subscribe to messages and notifications
  useEffect(() => {
    const unsubMessage = subscribe(handleMessage);
    const unsubNotification = subscribeToNotifications(handleNotification);

    return () => {
      unsubMessage();
      unsubNotification();
    };
  }, [subscribe, subscribeToNotifications, handleMessage, handleNotification]);

  // Mark chat as read when clicking on it
  const handleChatClick = useCallback((chatId: string) => {
    // Clear local unread count
    setUnreadCounts((prev) => ({
      ...prev,
      [chatId]: 0,
    }));

    // Notify backend that chat is opened
    notifyOpen(chatId);

    // Call API to mark as read
    fetch(`${BACKEND}/notifications/read/${chatId}`, {
      method: 'POST',
      credentials: 'include',
    }).catch(console.error);
  }, [notifyOpen]);

  // Mark current chat as read when pathname changes
  useEffect(() => {
    const match = pathname?.match(/^\/chat\/([^/]+)$/);
    if (match) {
      const chatId = match[1];
      handleChatClick(chatId);
    }
  }, [pathname, handleChatClick]);

  function handleChatStarted(chat: ChatSummary) {
    setChats((prev) => {
      if (prev.some((c) => c.chatId === chat.chatId)) return prev;
      return [chat, ...prev];
    });
  }

  return (
    <div style={{ display: 'flex', height: '100vh', overflow: 'hidden' }}>
      <Sidebar 
        chats={chats} 
        unreadCounts={unreadCounts}
        onNewChat={() => setShowSearch(true)} 
        onChatClick={handleChatClick}
      />
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
  );
}
