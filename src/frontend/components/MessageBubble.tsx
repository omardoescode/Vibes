'use client';

import type { Message } from '../lib/api';

interface MessageBubbleProps {
  message: Message;
  isMine: boolean;
}

/**
 * Backend stores bare paths like /filesupport/attachments/view/attachment-<uuid>-<filename>.
 * Prefix with /api/proxy so the Next.js route handler forwards the request to the backend.
 * Each path segment is percent-encoded so filenames with special characters (brackets,
 * spaces, parens, etc.) don't produce a malformed URL and a 400 from the backend.
 */
function proxyUrl(content: string): string {
  if (content.startsWith('/')) {
    const encoded = content
      .split('/')
      .map((segment) => encodeURIComponent(segment))
      .join('/');
    return `/api/proxy${encoded}`;
  }
  return content;
}

/**
 * Extract the human-readable filename from a backend file path.
 * Path format: /filesupport/{type}/view/attachment-<uuid>-<originalFilename>
 *                                    or: /filesupport/{type}/view/<uuid>-<originalFilename>
 * We take everything after the first UUID-looking segment.
 */
function extractFilename(content: string): string {
  const segment = content.split('/').pop() ?? content;
  // Strip leading "attachment-" or "file-" prefix if present
  const withoutPrefix = segment.replace(/^(attachment|file|profile)-/, '');
  // Strip the leading UUID (8-4-4-4-12 hex) and the dash after it
  const withoutUuid = withoutPrefix.replace(
    /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}-/i,
    ''
  );
  return withoutUuid || segment;
}

export default function MessageBubble({ message, isMine }: MessageBubbleProps) {
  const time = formatTime(message.timestamp);

  const isFile = message.type === 'FILE';

  return (
    <div
      className="animate-pop-in"
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: isMine ? 'flex-end' : 'flex-start',
        marginBottom: 6,
      }}
    >
      <div
        style={{
          maxWidth: '72%',
          padding: '10px 14px',
          borderRadius: isMine ? '18px 18px 6px 18px' : '18px 18px 18px 6px',
          background: isMine ? 'var(--accent)' : 'var(--bg-card)',
          color: isMine ? '#0d0d0d' : 'var(--text)',
          border: isMine ? 'none' : '2px solid var(--border)',
          boxShadow: 'var(--shadow)',
          fontSize: 15,
          fontWeight: 500,
          lineHeight: 1.5,
          wordBreak: 'break-word',
        }}
      >
        {message.type === 'TEXT' && <span>{message.content}</span>}
        {isFile && (
          <FileAttachment
            url={proxyUrl(message.content)}
            filename={extractFilename(message.content)}
            isMine={isMine}
          />
        )}
      </div>
      <span
        style={{
          fontSize: 11,
          color: 'var(--text-muted)',
          marginTop: 4,
          marginLeft: isMine ? 0 : 4,
          marginRight: isMine ? 4 : 0,
        }}
      >
        {time}
      </span>
    </div>
  );
}

function FileAttachment({
  url,
  filename,
  isMine,
}: {
  url: string;
  filename: string;
  isMine: boolean;
}) {
  return (
    <a
      href={url}
      download={filename}
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: 10,
        textDecoration: 'none',
        color: isMine ? '#0d0d0d' : 'var(--text)',
      }}
    >
      {/* File icon */}
      <svg
        width="28"
        height="28"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="square"
        strokeLinejoin="miter"
        style={{ flexShrink: 0, opacity: 0.85 }}
      >
        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
        <polyline points="14 2 14 8 20 8" />
      </svg>
      <span
        style={{
          fontSize: 13,
          fontWeight: 600,
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          whiteSpace: 'nowrap',
          maxWidth: 180,
        }}
      >
        {filename}
      </span>
    </a>
  );
}

function formatTime(iso: string): string {
  try {
    return new Date(iso).toLocaleTimeString(undefined, {
      hour: '2-digit',
      minute: '2-digit',
    });
  } catch {
    return '';
  }
}


