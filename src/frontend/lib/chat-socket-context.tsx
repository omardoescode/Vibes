'use client';

/**
 * ChatSocketContext — single STOMP connection shared across the entire chat shell.
 *
 * The layout owns the connection; the chat page consumes it.
 * This avoids the two-connection problem where messages were delivered to one
 * of two competing subscribers non-deterministically.
 *
 * Subscription: /user/{userId}/queue/messages
 * The backend delivers messages to this queue for both the sender and recipient.
 * The principal name on the STOMP session is the user's UUID string, remapped
 * from email by UserIdPrincipalInterceptor on the backend, so routing works correctly.
 */

import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
} from 'react';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type { Message } from './api';

const BACKEND = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080';

export type SocketStatus = 'connecting' | 'connected' | 'disconnected';

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

type MessageHandler = (msg: Message) => void;

type NotificationType = 'NEW_MESSAGE' | 'USER_ONLINE' | 'USER_OFFLINE' | 'TYPING_START' | 'TYPING_STOP';

export interface NotificationContext {
  recipientId: string;
  senderId: string;
  chatId: string;
  type: NotificationType;
  title: string;
  body: string;
  timestamp: string;
  payload: any;
}

type NotificationHandler = (notification: NotificationContext) => void;
type TypingHandler = (chatId: string, senderId: string, isTyping: boolean) => void;

interface ChatSocketState {
  status: SocketStatus;
  /** Subscribe to incoming messages; returns an unsubscribe function */
  subscribe: (handler: MessageHandler) => () => void;
  /** Subscribe to notifications; returns an unsubscribe function */
  subscribeToNotifications: (handler: NotificationHandler) => () => void;
  /** Subscribe to typing indicators; returns an unsubscribe function */
  subscribeToTyping: (handler: TypingHandler) => () => void;
  /** Send a text message via STOMP */
  sendMessage: (chatId: string, textContent: string) => void;
  /** Send typing indicator */
  sendTyping: (chatId: string, isTyping: boolean) => void;
  /** Notify the backend that this user has opened a chat */
  notifyOpen: (chatId: string) => void;
  /** Notify the backend that this user has closed the current chat */
  notifyClose: () => void;
}

// ---------------------------------------------------------------------------
// Context
// ---------------------------------------------------------------------------

const ChatSocketContext = createContext<ChatSocketState>({
  status: 'disconnected',
  subscribe: () => () => {},
  subscribeToNotifications: () => () => {},
  subscribeToTyping: () => () => {},
  sendMessage: () => {},
  sendTyping: () => {},
  notifyOpen: () => {},
  notifyClose: () => {},
});

// ---------------------------------------------------------------------------
// Provider
// ---------------------------------------------------------------------------

export function ChatSocketProvider({
  userId,
  children,
}: {
  userId: string | null;
  children: React.ReactNode;
}) {
  const clientRef = useRef<Client | null>(null);
  const [status, setStatus] = useState<SocketStatus>('disconnected');

  // Fan-out: set of active handlers
  const handlersRef = useRef<Set<MessageHandler>>(new Set());
  const notificationHandlersRef = useRef<Set<NotificationHandler>>(new Set());
  const typingHandlersRef = useRef<Set<TypingHandler>>(new Set());

  useEffect(() => {
    if (!userId) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(`${BACKEND}/ws`) as WebSocket,
      reconnectDelay: 5000,

      onConnect: () => {
        setStatus('connected');
        // Subscribe to the user-destination queue for messages.
        // The path must be /user/queue/messages — NOT /user/{id}/queue/messages.
        // Spring's UserDestinationMessageHandler rewrites convertAndSendToUser(uuid, "/queue/messages")
        // to /user/{uuid}/queue/messages internally, but clients must subscribe to the
        // un-prefixed form so Spring can match the session principal and route correctly.
        client.subscribe(
          `/user/queue/messages`,
          (frame: IMessage) => {
            try {
              const msg: Message = JSON.parse(frame.body);
              handlersRef.current.forEach((h) => h(msg));
            } catch {
              console.error('[socket] failed to parse message', frame.body);
            }
          }
        );

        // Subscribe to notifications queue
        console.log('[socket] subscribing to /user/queue/notifications');
        client.subscribe(
          `/user/queue/notifications`,
          (frame: IMessage) => {
            console.log('[socket] notification received:', frame.body);
            try {
              const notification: NotificationContext = JSON.parse(frame.body);
              console.log('[socket] parsed notification:', notification);
              notificationHandlersRef.current.forEach((h) => h(notification));
            } catch (err) {
              console.error('[socket] failed to parse notification', frame.body, err);
            }
          }
        );
      },

      onDisconnect: () => setStatus('disconnected'),
      onStompError: (frame) => {
        console.error('[socket] STOMP error', frame);
        setStatus('disconnected');
      },
      onWebSocketError: (event) => {
        console.error('[socket] WebSocket error', event);
        setStatus('disconnected');
      },
    });

    setStatus('connecting');
    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
      setStatus('disconnected');
    };
  }, [userId]); // reconnect if the logged-in user changes

  const subscribe = useCallback((handler: MessageHandler) => {
    handlersRef.current.add(handler);
    return () => {
      handlersRef.current.delete(handler);
    };
  }, []);

  const subscribeToNotifications = useCallback((handler: NotificationHandler) => {
    notificationHandlersRef.current.add(handler);
    return () => {
      notificationHandlersRef.current.delete(handler);
    };
  }, []);

  const subscribeToTyping = useCallback((handler: TypingHandler) => {
    typingHandlersRef.current.add(handler);
    return () => {
      typingHandlersRef.current.delete(handler);
    };
  }, []);

  const sendMessage = useCallback((chatId: string, textContent: string) => {
    const client = clientRef.current;
    if (!client?.connected) {
      console.warn('[socket] tried to send while disconnected');
      return;
    }
    client.publish({
      destination: '/app/chat.send',
      body: JSON.stringify({ chatId, textContent }),
    });
  }, []);

  const sendTyping = useCallback((chatId: string, isTyping: boolean) => {
    const client = clientRef.current;
    if (!client?.connected) {
      console.warn('[socket] tried to send typing while disconnected');
      return;
    }
    client.publish({
      destination: '/app/chat.typing',
      body: JSON.stringify({ chatId, isTyping }),
    });
  }, []);

  const notifyOpen = useCallback((chatId: string) => {
    clientRef.current?.publish({
      destination: '/app/chat.open',
      body: JSON.stringify({ chatId }),
    });
  }, []);

  const notifyClose = useCallback(() => {
    clientRef.current?.publish({
      destination: '/app/chat.close',
      body: '{}',
    });
  }, []);

  return (
    <ChatSocketContext.Provider 
      value={{ 
        status, 
        subscribe, 
        subscribeToNotifications,
        subscribeToTyping,
        sendMessage, 
        sendTyping,
        notifyOpen, 
        notifyClose 
      }}
    >
      {children}
    </ChatSocketContext.Provider>
  );
}

// ---------------------------------------------------------------------------
// Hook
// ---------------------------------------------------------------------------

export function useChatSocket(): ChatSocketState {
  return useContext(ChatSocketContext);
}
