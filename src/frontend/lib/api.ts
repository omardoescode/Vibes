// ---------------------------------------------------------------------------
// Vibes API client — all REST communication with the Spring Boot backend
// Uses session cookies (JSESSIONID) — no JWT
//
// In the browser we route through /api/proxy/... — a Next.js route handler
// that faithfully forwards ALL response headers (including Set-Cookie).
// Next.js rewrites strip Set-Cookie, breaking session auth, so we can't use
// those for auth endpoints.
//
// In server-side contexts (SSR) we hit the backend directly.
// ---------------------------------------------------------------------------

const BACKEND = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080';

// Browser: go through the proxy route handler so Set-Cookie is preserved.
// SSR: hit the backend directly (no browser cookie jar involved).
const BASE =
  typeof window === 'undefined'
    ? BACKEND
    : '/api/proxy';

// ---------------------------------------------------------------------------
// Shared types
// ---------------------------------------------------------------------------

export interface UserProfile {
  id: string;
  username: string;
  profilePictureUrl: string | null;
  status: 'online' | 'offline' | string;
}

export interface ChatSummary {
  chatId: string;
  otherUserId: string;
  otherUsername: string;
  otherUserProfilePicture: string | null;
  createdAt: string;
}

export type MessageType = 'TEXT' | 'FILE';

export interface Message {
  id: string;
  chatId: string;
  senderId: string;
  timestamp: string;
  content: string;
  type: MessageType;
}

// ---------------------------------------------------------------------------
// Core fetch helper
// ---------------------------------------------------------------------------

async function apiFetch<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    credentials: 'include',          // always send/receive cookies
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  });

  if (!res.ok) {
    let message = `HTTP ${res.status}`;
    try {
      const body = await res.json();
      message = body.error ?? body.message ?? message;
    } catch {
      // ignore parse failure
    }
    throw new Error(message);
  }

  // 204 / no content
  const text = await res.text();
  return text ? (JSON.parse(text) as T) : (undefined as unknown as T);
}

// ---------------------------------------------------------------------------
// Auth
// ---------------------------------------------------------------------------

export async function register(
  username: string,
  email: string,
  password: string
): Promise<UserProfile> {
  return apiFetch<UserProfile>('/auth/register', {
    method: 'POST',
    body: JSON.stringify({ username, email, password }),
  });
}

export async function login(email: string, password: string): Promise<void> {
  await apiFetch<unknown>('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  });
}

export async function logout(): Promise<void> {
  await apiFetch<unknown>('/auth/logout', { method: 'POST' });
}

/** Returns the authenticated user's full profile */
export async function getMe(): Promise<UserProfile> {
  return apiFetch<UserProfile>('/auth/me');
}

/**
 * Upload a new profile picture for the current user.
 * Returns the updated UserProfile with the new profilePictureUrl.
 */
export async function uploadAvatar(file: File): Promise<UserProfile> {
  const fd = new FormData();
  fd.append('file', file);
  const res = await fetch(`${BASE}/auth/me/avatar`, {
    method: 'POST',
    credentials: 'include',
    body: fd,
  });
  if (!res.ok) {
    let message = `HTTP ${res.status}`;
    try {
      const body = await res.json();
      message = body.error ?? body.message ?? message;
    } catch { /* ignore */ }
    throw new Error(message);
  }
  return res.json() as Promise<UserProfile>;
}

export async function searchUsers(query: string): Promise<UserProfile[]> {
  return apiFetch<UserProfile[]>(
    `/auth/users/search?query=${encodeURIComponent(query)}`
  );
}

export async function getUserById(userId: string): Promise<UserProfile> {
  return apiFetch<UserProfile>(`/auth/users/${userId}`);
}

// ---------------------------------------------------------------------------
// Chats
// ---------------------------------------------------------------------------

export async function startChat(targetUserId: string): Promise<ChatSummary> {
  return apiFetch<ChatSummary>(
    `/chats/start?targetUserId=${encodeURIComponent(targetUserId)}`,
    { method: 'POST' }
  );
}

export async function listChats(): Promise<ChatSummary[]> {
  return apiFetch<ChatSummary[]>('/chats');
}

// ---------------------------------------------------------------------------
// Messages
// ---------------------------------------------------------------------------

export async function getChatMessages(chatId: string): Promise<Message[]> {
  return apiFetch<Message[]>(`/messages/chat/${chatId}`);
}

/**
 * Upload a media file as a message.
 * Returns the saved Message object.
 */
export async function sendMediaMessage(
  chatId: string,
  senderId: string,
  file: File
): Promise<Message> {
  const fd = new FormData();
  fd.append('chatId', chatId);
  fd.append('senderId', senderId);
  fd.append('file', file);

  const res = await fetch(`${BASE}/messages/media`, {
    method: 'POST',
    credentials: 'include',
    body: fd,
    // Do NOT set Content-Type — browser sets it with the boundary
  });

  if (!res.ok) {
    let message = `HTTP ${res.status}`;
    try {
      const body = await res.json();
      message = body.error ?? body.message ?? message;
    } catch {
      // ignore
    }
    throw new Error(message);
  }

  return res.json() as Promise<Message>;
}

// ---------------------------------------------------------------------------
// File URLs (pass through for <img src> / <a href>)
// ---------------------------------------------------------------------------

/**
 * Prefix a bare backend path (e.g. /filesupport/profiles/view/profile-<uuid>)
 * with /api/proxy so it is served through the Next.js proxy route handler.
 * If the value is already absolute or already proxied, return it unchanged.
 */
export function toProxyUrl(url: string | null | undefined): string | null {
  if (!url) return null;
  if (url.startsWith('/api/proxy')) return url;
  if (url.startsWith('/')) return `/api/proxy${url}`;
  return url; // already absolute URL
}

export function profilePictureUrl(fileId: string): string {
  return `/api/proxy/filesupport/profiles/view/${fileId}`;
}

export function fileDownloadUrl(fileId: string): string {
  return `/api/proxy/filesupport/files/download/${fileId}`;
}
