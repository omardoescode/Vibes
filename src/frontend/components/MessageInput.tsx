'use client';

import { useRef, useState } from 'react';

interface MessageInputProps {
  onSend: (text: string) => void;
  onTyping?: () => void;
  onFileSelect?: (file: File) => void;
  disabled?: boolean;
}

export default function MessageInput({ onSend, onTyping, onFileSelect, disabled }: MessageInputProps) {
  const [text, setText] = useState('');
  const fileRef = useRef<HTMLInputElement>(null);
  const typingTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const trimmed = text.trim();
    if (!trimmed || disabled) return;
    onSend(trimmed);
    setText('');
  }

  function handleKeyDown(e: React.KeyboardEvent<HTMLTextAreaElement>) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit(e as unknown as React.FormEvent);
    }
  }

  function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (file && onFileSelect) {
      onFileSelect(file);
    }
    // reset so the same file can be re-selected
    e.target.value = '';
  }

  return (
    <form
      onSubmit={handleSubmit}
      style={{
        display: 'flex',
        alignItems: 'flex-end',
        gap: 10,
        padding: '14px 16px',
        borderTop: '2px solid var(--border)',
        background: 'var(--bg-card)',
      }}
    >
      {/* File attach button */}
      {onFileSelect && (
        <>
          <input
            ref={fileRef}
            type="file"
            accept="image/*,video/*,audio/*,.pdf,.doc,.docx,.txt"
            style={{ display: 'none' }}
            onChange={handleFileChange}
          />
          <button
            type="button"
            onClick={() => fileRef.current?.click()}
            disabled={disabled}
            title="Attach file"
            style={{
              width: 40,
              height: 40,
              borderRadius: '50%',
              border: '2px solid var(--border)',
              background: 'var(--bg)',
              color: 'var(--text-muted)',
              fontSize: 18,
              cursor: disabled ? 'not-allowed' : 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexShrink: 0,
            }}
          >
            📎
          </button>
        </>
      )}

      {/* Text area */}
      <textarea
        value={text}
        onChange={(e) => {
          setText(e.target.value);
          // Notify typing when user types
          if (onTyping && e.target.value.trim()) {
            onTyping();
          }
        }}
        onKeyDown={handleKeyDown}
        placeholder="Send a message… (Enter to send, Shift+Enter for newline)"
        disabled={disabled}
        rows={1}
        style={{
          flex: 1,
          resize: 'none',
          padding: '10px 14px',
          border: '2px solid var(--border)',
          borderRadius: 'var(--radius-sm)',
          background: 'var(--bg)',
          color: 'var(--text)',
          fontSize: 15,
          fontFamily: 'Rajdhani, sans-serif',
          fontWeight: 500,
          outline: 'none',
          lineHeight: 1.5,
          maxHeight: 120,
          overflowY: 'auto',
        }}
      />

      {/* Send button */}
      <button
        type="submit"
        disabled={!text.trim() || disabled}
        style={{
          width: 42,
          height: 42,
          borderRadius: '50%',
          background: text.trim() && !disabled ? 'var(--accent)' : 'var(--yellow-800)',
          border: 'none',
          color: text.trim() && !disabled ? '#0d0d0d' : 'var(--text-muted)',
          fontSize: 18,
          cursor: text.trim() && !disabled ? 'pointer' : 'not-allowed',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          boxShadow: text.trim() && !disabled ? 'var(--shadow)' : 'none',
          transition: 'background 0.15s, transform 0.1s',
          flexShrink: 0,
        }}
      >
        ➤
      </button>
    </form>
  );
}
