'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import { useParams } from 'next/navigation';
import { getChatMessages, getUserById, sendMediaMessage, toProxyUrl } from '../../../../lib/api';
import type { Message, UserProfile } from '../../../../lib/api';
import { useAuth } from '../../../../lib/auth-context';
import { useChatSocket } from '../../../../lib/chat-socket-context';
import MessageBubble from '../../../../components/MessageBubble';
import MessageInput from '../../../../components/MessageInput';

// ---------------------------------------------------------------------------
// Browser notification helper
// ---------------------------------------------------------------------------

function requestNotificationPermission() {
  if (typeof Notification !== 'undefined' && Notification.permission === 'default') {
    Notification.requestPermission();
  }
}

function showNotification(title: string, body: string) {
  if (typeof Notification === 'undefined') return;
  if (Notification.permission !== 'granted') return;
  // Don't notify if the tab is focused
  if (document.visibilityState === 'visible') return;
  try {
    new Notification(title, { body, icon: '/favicon.ico' });
  } catch {
    // Some browsers block notifications in certain contexts
  }
}

// ---------------------------------------------------------------------------
// ChatPage
// ---------------------------------------------------------------------------

export default function ChatPage() {
  const { chatId } = useParams<{ chatId: string }>();
  const { user } = useAuth();
  const { status, subscribe, subscribeToTyping, sendMessage, sendTyping, notifyOpen, notifyClose } = useChatSocket();

  const [messages, setMessages] = useState<Message[]>([]);
  const [otherUser, setOtherUser] = useState<UserProfile | null>(null);
  const [loadingMsgs, setLoadingMsgs] = useState(true);
  const [uploadError, setUploadError] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const typingTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  const bottomRef = useRef<HTMLDivElement>(null);

  // Ask for notification permission once
  useEffect(() => {
    requestNotificationPermission();
  }, []);

  // ---- Notify backend of open/close chat ----
  useEffect(() => {
    if (!chatId) return;
    notifyOpen(chatId);
    return () => notifyClose();
  }, [chatId, notifyOpen, notifyClose]);

  // ---- Load message history ----
  useEffect(() => {
    if (!chatId) return;
    setLoadingMsgs(true);
    setMessages([]);
    getChatMessages(chatId)
      .then(setMessages)
      .catch(console.error)
      .finally(() => setLoadingMsgs(false));
  }, [chatId]);

  // ---- Load other user's profile ----
  useEffect(() => {
    if (!user || messages.length === 0) return;
    const otherId = messages.find((m) => m.senderId !== user.id)?.senderId;
    if (!otherId) return;
    getUserById(otherId).then(setOtherUser).catch(() => {});
  }, [messages, user]);

  // ---- Auto-scroll to bottom ----
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // ---- Subscribe to incoming messages ----
  useEffect(() => {
    const unsub = subscribe((msg: Message) => {
      if (msg.chatId !== chatId) return;

      setMessages((prev) => {
        // Deduplicate: the optimistic message has a temp id starting with 'optimistic-'
        // The real message arrives from the server with a proper UUID — replace it.
        const withoutOptimistic = prev.filter(
          (m) => !(m.id.startsWith('optimistic-') && m.content === msg.content && m.senderId === msg.senderId)
        );
        if (withoutOptimistic.some((m) => m.id === msg.id)) return withoutOptimistic;
        return [...withoutOptimistic, msg];
      });

      // Notify if message is from the other person
      if (user && msg.senderId !== user.id) {
        showNotification(
          otherUser?.username ?? 'New message',
          msg.type === 'TEXT' ? msg.content : '📎 Attachment'
        );
      }
    });
    return unsub;
  }, [subscribe, chatId, user, otherUser]);

  // ---- Subscribe to typing indicators ----
  useEffect(() => {
    const unsub = subscribeToTyping((incomingChatId, senderId, typing) => {
      if (incomingChatId !== chatId) return;
      if (user && senderId === user.id) return; // Don't show own typing
      
      setIsTyping(typing);
      
      // Clear typing after 3 seconds as a fallback
      if (typing) {
        if (typingTimeoutRef.current) {
          clearTimeout(typingTimeoutRef.current);
        }
        typingTimeoutRef.current = setTimeout(() => {
          setIsTyping(false);
        }, 3000);
      }
    });
    return unsub;
  }, [subscribeToTyping, chatId, user]);

  // ---- Send text (optimistic) ----
  function handleSend(text: string) {
    if (!chatId || !user) return;

    // Append optimistic message immediately
    const optimistic: Message = {
      id: `optimistic-${Date.now()}`,
      chatId,
      senderId: user.id,
      content: text,
      type: 'TEXT',
      timestamp: new Date().toISOString(),
    };
    setMessages((prev) => [...prev, optimistic]);

    sendMessage(chatId, text);
    
    // Clear typing indicator
    sendTyping(chatId, false);
  }
  
  // ---- Handle typing ----
  function handleTyping() {
    if (!chatId || !user) return;
    sendTyping(chatId, true);
    
    // Auto-clear typing after 2 seconds of inactivity
    if (typingTimeoutRef.current) {
      clearTimeout(typingTimeoutRef.current);
    }
    typingTimeoutRef.current = setTimeout(() => {
      sendTyping(chatId, false);
    }, 2000);
  }

  // ---- Upload media ----
  async function handleFileSelect(file: File) {
    if (!user || !chatId) return;
    setUploadError('');
    try {
      const msg = await sendMediaMessage(chatId, user.id, file);
      setMessages((prev) => {
        if (prev.some((m) => m.id === msg.id)) return prev;
        return [...prev, msg];
      });
    } catch (err: unknown) {
      setUploadError(err instanceof Error ? err.message : 'Upload failed');
    }
  }

  const otherUsername = otherUser?.username ?? '…';
  const socketConnected = status === 'connected';

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', overflow: 'hidden' }}>
      {/* Chat header */}
      <div
        style={{
          padding: '14px 20px',
          borderBottom: '2px solid var(--border)',
          background: 'var(--bg-card)',
          display: 'flex',
          alignItems: 'center',
          gap: 12,
          flexShrink: 0,
        }}
      >
        <div
          style={{
            width: 40,
            height: 40,
            borderRadius: '50%',
            background: 'var(--yellow-700)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontFamily: 'Press Start 2P, monospace',
            fontSize: 18,
            color: 'var(--accent)',
            border: '2px solid var(--border)',
            flexShrink: 0,
            overflow: 'hidden',
          }}
        >
          {otherUser?.profilePictureUrl ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img
              src={toProxyUrl(otherUser.profilePictureUrl)!}
              alt={otherUsername}
              style={{ width: '100%', height: '100%', objectFit: 'cover' }}
            />
          ) : (
            otherUsername[0]?.toUpperCase() ?? '?'
          )}
        </div>

        <div style={{ flex: 1 }}>
          <div style={{ fontWeight: 700, fontSize: 16, color: 'var(--text)' }}>
            {otherUsername}
          </div>
          <div
            style={{
              fontSize: 12,
              color: isTyping ? 'var(--accent)' : socketConnected ? '#22c55e' : 'var(--text-muted)',
              fontWeight: 600,
              marginTop: 1,
            }}
          >
            {isTyping 
              ? 'typing…' 
              : socketConnected 
                ? 'Connected' 
                : status === 'connecting' 
                  ? 'Connecting…' 
                  : 'Disconnected'}
          </div>
        </div>
      </div>

      {/* Messages */}
      <div
        style={{
          flex: 1,
          overflowY: 'auto',
          padding: '20px 20px 8px',
          display: 'flex',
          flexDirection: 'column',
          gap: 2,
        }}
      >
        {loadingMsgs && (
          <div style={{ textAlign: 'center', color: 'var(--text-muted)', fontWeight: 600, paddingTop: 40 }}>
            Loading messages…
          </div>
        )}

        {!loadingMsgs && messages.length === 0 && (
          <div
            style={{
              flex: 1,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              gap: 8,
              color: 'var(--text-muted)',
            }}
          >
            <span style={{ fontSize: 40 }}>💬</span>
            <span style={{ fontWeight: 700, fontSize: 15 }}>No messages yet. Say hi!</span>
          </div>
        )}

        {messages.map((msg) => (
          <MessageBubble
            key={msg.id}
            message={msg}
            isMine={msg.senderId === user?.id}
          />
        ))}

        <div ref={bottomRef} />
      </div>

      {uploadError && (
        <div
          style={{
            padding: '8px 16px',
            background: '#fff3f0',
          color: 'var(--pop)',
          fontSize: 13,
          fontWeight: 600,
          borderTop: '1px solid #c0390a',
          }}
        >
          {uploadError}
        </div>
      )}

      <MessageInput
        onSend={handleSend}
        onTyping={handleTyping}
        onFileSelect={handleFileSelect}
        disabled={!socketConnected}
      />
    </div>
  );
}
