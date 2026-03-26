'use client';

import { useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { Sparkles } from 'lucide-react';
import { searchChatMessages, searchGlobalMessages, type Message } from '../lib/api';
import { useAuth } from '../lib/auth-context';

interface SearchModalProps {
  chatId?: string;
  isOpen: boolean;
  onClose: () => void;
}

interface SearchResult extends Message {
  contextBefore?: string;
  contextAfter?: string;
  highlightedContent?: string;
}

export default function SearchModal({ chatId, isOpen, onClose }: SearchModalProps) {
  const router = useRouter();
  const { user } = useAuth();
  const [query, setQuery] = useState('');
  const [fuzzy, setFuzzy] = useState(false);
  const [scope, setScope] = useState<'chat' | 'global'>(chatId ? 'chat' : 'global');
  const [results, setResults] = useState<SearchResult[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);
  const [error, setError] = useState('');

  // Debounce search
  const debouncedSearch = useCallback(
    debounce(async (searchQuery: string, searchFuzzy: boolean, searchScope: string) => {
      if (!searchQuery.trim()) {
        setResults([]);
        setHasSearched(false);
        return;
      }

      setIsSearching(true);
      setError('');

      try {
        let messages: Message[];
        
        if (searchScope === 'chat' && chatId) {
          messages = await searchChatMessages(chatId, searchQuery, searchFuzzy);
        } else {
          messages = await searchGlobalMessages(searchQuery, searchFuzzy);
        }

        // Process results to add context
        const processedResults = messages.map((msg) => {
          const content = msg.content;
          const queryLower = searchQuery.toLowerCase();
          const contentLower = content.toLowerCase();
          const matchIndex = contentLower.indexOf(queryLower);

          if (matchIndex === -1) {
            return {
              ...msg,
              highlightedContent: content,
            };
          }

          const words = content.split(/\s+/);
          let wordIndex = 0;
          let charCount = 0;
          
          for (let i = 0; i < words.length; i++) {
            if (charCount + words[i].length > matchIndex) {
              wordIndex = i;
              break;
            }
            charCount += words[i].length + 1;
          }

          const startWord = Math.max(0, wordIndex - 3);
          const endWord = Math.min(words.length, wordIndex + 4);
          
          const contextWords = words.slice(startWord, endWord);
          const matchWordIndex = wordIndex - startWord;
          
          const highlighted = contextWords.map((word, idx) => {
            if (idx === matchWordIndex) {
              return `<mark style="background: var(--accent); color: #0d0d0d; padding: 0 2px; border-radius: 2px;">${escapeHtml(word)}</mark>`;
            }
            return escapeHtml(word);
          }).join(' ');

          return {
            ...msg,
            highlightedContent: highlighted,
            contextBefore: startWord > 0 ? '...' : '',
            contextAfter: endWord < words.length ? '...' : '',
          };
        });

        setResults(processedResults);
        setHasSearched(true);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Search failed');
        setResults([]);
      } finally {
        setIsSearching(false);
      }
    }, 300),
    [chatId]
  );

  function handleQueryChange(e: React.ChangeEvent<HTMLInputElement>) {
    const newQuery = e.target.value;
    setQuery(newQuery);
    debouncedSearch(newQuery, fuzzy, scope);
  }

  function handleNavigateToMessage(message: Message) {
    if (message.chatId !== chatId) {
      router.push(`/chat/${message.chatId}`);
    }
    onClose();
  }

  if (!isOpen) return null;

  return (
    <div style={{
      position: 'fixed',
      inset: 0,
      backgroundColor: 'rgba(0, 0, 0, 0.7)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 1000,
    }}>
      <div style={{
        backgroundColor: 'var(--bg-card)',
        border: '2px solid var(--border)',
        borderRadius: 'var(--radius-md)',
        padding: '24px',
        width: '90%',
        maxWidth: 600,
        maxHeight: '90vh',
        display: 'flex',
        flexDirection: 'column',
      }}>
        <h2 style={{
          margin: '0 0 20px 0',
          color: 'var(--accent)',
          fontSize: 20,
          fontWeight: 700,
        }}>
          Search Messages
        </h2>

        {/* Search Input */}
        <div style={{ marginBottom: 16 }}>
          <input
            type="text"
            value={query}
            onChange={handleQueryChange}
            placeholder="Search for messages..."
            autoFocus
            style={{
              width: '100%',
              padding: '12px 16px',
              border: '2px solid var(--border)',
              borderRadius: 'var(--radius-sm)',
              background: 'var(--bg)',
              color: 'var(--text)',
              fontSize: 15,
              fontFamily: 'Rajdhani, sans-serif',
              fontWeight: 500,
              outline: 'none',
            }}
          />
        </div>

        {/* Options Row */}
        <div style={{
          display: 'flex',
          gap: 12,
          marginBottom: 16,
          alignItems: 'center',
          flexWrap: 'wrap',
        }}>
          {/* Scope Toggle */}
          {chatId && (
            <div style={{ display: 'flex', gap: 8 }}>
              {[
                { key: 'chat', label: 'This Chat' },
                { key: 'global', label: 'All My Chats' },
              ].map((opt) => (
                <button
                  key={opt.key}
                  onClick={() => {
                    setScope(opt.key as 'chat' | 'global');
                    debouncedSearch(query, fuzzy, opt.key);
                  }}
                  style={{
                    padding: '6px 12px',
                    border: '2px solid var(--border)',
                    borderRadius: 'var(--radius-sm)',
                    background: scope === opt.key ? 'var(--accent)' : 'var(--bg)',
                    color: scope === opt.key ? '#0d0d0d' : 'var(--text)',
                    cursor: 'pointer',
                    fontWeight: 600,
                    fontSize: 13,
                  }}
                >
                  {opt.label}
                </button>
              ))}
            </div>
          )}

          <div style={{ flex: 1 }} />

          {/* Fuzzy Toggle */}
          <button
            onClick={() => {
              setFuzzy(!fuzzy);
              debouncedSearch(query, !fuzzy, scope);
            }}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 6,
              padding: '6px 12px',
              border: '2px solid var(--border)',
              borderRadius: 'var(--radius-sm)',
              background: fuzzy ? 'var(--yellow-800)' : 'var(--bg)',
              color: fuzzy ? 'var(--accent)' : 'var(--text-muted)',
              cursor: 'pointer',
              fontWeight: 600,
              fontSize: 13,
              transition: 'all 0.15s ease',
            }}
          >
            <Sparkles size={14} />
            <span>Fuzzy Search</span>
          </button>
        </div>

        {/* Error Message */}
        {error && (
          <div style={{
            padding: '10px 12px',
            backgroundColor: '#fff3f0',
            color: 'var(--pop)',
            borderRadius: 'var(--radius-sm)',
            marginBottom: 16,
            fontSize: 13,
            fontWeight: 600,
          }}>
            {error}
          </div>
        )}

        {/* Results */}
        <div style={{
          flex: 1,
          overflowY: 'auto',
          border: '2px solid var(--border)',
          borderRadius: 'var(--radius-sm)',
          background: 'var(--bg)',
          maxHeight: 300,
        }}>
          {isSearching ? (
            <div style={{
              padding: 40,
              textAlign: 'center',
              color: 'var(--text-muted)',
              fontWeight: 600,
            }}>
              Searching...
            </div>
          ) : hasSearched && results.length === 0 ? (
            <div style={{
              padding: 40,
              textAlign: 'center',
              color: 'var(--text-muted)',
              fontWeight: 600,
            }}>
              No messages found
            </div>
          ) : (
            <div>
              {results.map((result) => (
                <button
                  key={result.id}
                  onClick={() => handleNavigateToMessage(result)}
                  style={{
                    width: '100%',
                    padding: '12px 16px',
                    border: 'none',
                    borderBottom: '1px solid var(--border)',
                    background: 'transparent',
                    textAlign: 'left',
                    cursor: 'pointer',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: 4,
                  }}
                >
                  <div style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                  }}>
                    <span style={{
                      fontWeight: 700,
                      fontSize: 13,
                      color: 'var(--text)',
                    }}>
                      {result.senderId === user?.id ? 'You' : result.senderId}
                    </span>
                    <span style={{
                      fontSize: 11,
                      color: 'var(--text-muted)',
                    }}>
                      {new Date(result.timestamp).toLocaleString()}
                    </span>
                  </div>
                  {result.chatId !== chatId && (
                    <span style={{
                      fontSize: 11,
                      color: 'var(--accent)',
                      fontWeight: 600,
                    }}>
                      in {result.chatId}
                    </span>
                  )}
                  <div
                    style={{
                      fontSize: 14,
                      color: 'var(--text)',
                      lineHeight: 1.5,
                    }}
                    dangerouslySetInnerHTML={{
                      __html: `${result.contextBefore || ''} ${result.highlightedContent || result.content} ${result.contextAfter || ''}`,
                    }}
                  />
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Close Button */}
        <div style={{
          display: 'flex',
          justifyContent: 'flex-end',
          marginTop: 16,
        }}>
          <button
            onClick={onClose}
            style={{
              padding: '10px 20px',
              border: '2px solid var(--border)',
              borderRadius: 'var(--radius-sm)',
              background: 'var(--bg)',
              color: 'var(--text)',
              cursor: 'pointer',
              fontWeight: 600,
              fontSize: 14,
            }}
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
}

// Utility function for debouncing
type SearchFunction = (query: string, fuzzy: boolean, scope: string) => Promise<void>;

function debounce(
  func: SearchFunction,
  wait: number
): (query: string, fuzzy: boolean, scope: string) => void {
  let timeout: NodeJS.Timeout;
  return (query: string, fuzzy: boolean, scope: string) => {
    clearTimeout(timeout);
    timeout = setTimeout(() => func(query, fuzzy, scope), wait);
  };
}

// Utility function to escape HTML
function escapeHtml(text: string): string {
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}