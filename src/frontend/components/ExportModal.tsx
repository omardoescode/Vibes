'use client';

import { useState } from 'react';
import { exportChat, exportChatDateRange, exportChatBySenders, type ExportFormat } from '../lib/api';

interface ExportModalProps {
  chatId: string;
  isOpen: boolean;
  onClose: () => void;
  chatMembers?: { userId: string; username: string }[];
}

type ExportType = 'full' | 'dateRange' | 'senders';

export default function ExportModal({ chatId, isOpen, onClose, chatMembers }: ExportModalProps) {
  const [exportType, setExportType] = useState<ExportType>('full');
  const [format, setFormat] = useState<ExportFormat>('json');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [selectedSenders, setSelectedSenders] = useState<string[]>([]);
  const [isExporting, setIsExporting] = useState(false);
  const [error, setError] = useState('');

  if (!isOpen) return null;

  async function handleExport() {
    setIsExporting(true);
    setError('');

    try {
      let blob: Blob;
      let filename: string;

      switch (exportType) {
        case 'dateRange':
          if (!startDate || !endDate) {
            setError('Please select both start and end dates');
            setIsExporting(false);
            return;
          }
          blob = await exportChatDateRange(chatId, startDate, endDate, format);
          filename = `chat-${chatId}-${startDate}-to-${endDate}.${format}`;
          break;
        case 'senders':
          if (selectedSenders.length === 0) {
            setError('Please select at least one sender');
            setIsExporting(false);
            return;
          }
          blob = await exportChatBySenders(chatId, selectedSenders, format);
          filename = `chat-${chatId}-filtered.${format}`;
          break;
        default:
          blob = await exportChat(chatId, format);
          filename = `chat-${chatId}.${format}`;
      }

      // Trigger download
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);

      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Export failed');
    } finally {
      setIsExporting(false);
    }
  }

  function toggleSender(userId: string) {
    setSelectedSenders(prev => 
      prev.includes(userId) 
        ? prev.filter(id => id !== userId)
        : [...prev, userId]
    );
  }

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
        maxWidth: 500,
        maxHeight: '90vh',
        overflowY: 'auto',
      }}>
        <h2 style={{
          margin: '0 0 20px 0',
          color: 'var(--accent)',
          fontSize: 20,
          fontWeight: 700,
        }}>
          Export Chat
        </h2>

        {/* Export Type Tabs */}
        <div style={{
          display: 'flex',
          gap: 8,
          marginBottom: 20,
          borderBottom: '2px solid var(--border)',
          paddingBottom: 12,
        }}>
          {[
            { key: 'full', label: 'Full Chat' },
            { key: 'dateRange', label: 'Date Range' },
            { key: 'senders', label: 'Senders' },
          ].map((tab) => (
            <button
              key={tab.key}
              onClick={() => setExportType(tab.key as ExportType)}
              style={{
                padding: '8px 16px',
                border: '2px solid var(--border)',
                borderRadius: 'var(--radius-sm)',
                background: exportType === tab.key ? 'var(--accent)' : 'var(--bg)',
                color: exportType === tab.key ? '#0d0d0d' : 'var(--text)',
                cursor: 'pointer',
                fontWeight: 600,
                fontSize: 14,
              }}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* Format Selection */}
        <div style={{ marginBottom: 20 }}>
          <label style={{
            display: 'block',
            marginBottom: 8,
            fontWeight: 600,
            color: 'var(--text)',
            fontSize: 14,
          }}>
            Format
          </label>
          <div style={{ display: 'flex', gap: 12 }}>
            {[
              { key: 'json', label: 'JSON' },
              { key: 'csv', label: 'CSV' },
            ].map((fmt) => (
              <label
                key={fmt.key}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 6,
                  cursor: 'pointer',
                  padding: '8px 12px',
                  border: '2px solid var(--border)',
                  borderRadius: 'var(--radius-sm)',
                  background: format === fmt.key ? 'var(--yellow-800)' : 'transparent',
                }}
              >
                <input
                  type="radio"
                  name="format"
                  value={fmt.key}
                  checked={format === fmt.key}
                  onChange={(e) => setFormat(e.target.value as ExportFormat)}
                  style={{ cursor: 'pointer' }}
                />
                <span style={{
                  fontWeight: 600,
                  color: 'var(--text)',
                  fontSize: 14,
                }}>
                  {fmt.label}
                </span>
              </label>
            ))}
          </div>
        </div>

        {/* Date Range Fields */}
        {exportType === 'dateRange' && (
          <div style={{ marginBottom: 20 }}>
            <label style={{
              display: 'block',
              marginBottom: 8,
              fontWeight: 600,
              color: 'var(--text)',
              fontSize: 14,
            }}>
              Date Range
            </label>
            <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
              <input
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                style={{
                  flex: 1,
                  padding: '10px 12px',
                  border: '2px solid var(--border)',
                  borderRadius: 'var(--radius-sm)',
                  background: 'var(--bg)',
                  color: 'var(--text)',
                  fontSize: 14,
                }}
              />
              <span style={{ color: 'var(--text-muted)', fontWeight: 600 }}>to</span>
              <input
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                style={{
                  flex: 1,
                  padding: '10px 12px',
                  border: '2px solid var(--border)',
                  borderRadius: 'var(--radius-sm)',
                  background: 'var(--bg)',
                  color: 'var(--text)',
                  fontSize: 14,
                }}
              />
            </div>
          </div>
        )}

        {/* Sender Selection */}
        {exportType === 'senders' && (
          <div style={{ marginBottom: 20 }}>
            <label style={{
              display: 'block',
              marginBottom: 8,
              fontWeight: 600,
              color: 'var(--text)',
              fontSize: 14,
            }}>
              Select Senders
            </label>
            <div style={{
              maxHeight: 200,
              overflowY: 'auto',
              border: '2px solid var(--border)',
              borderRadius: 'var(--radius-sm)',
              padding: 8,
            }}>
              {chatMembers && chatMembers.length > 0 ? (
                chatMembers.map((member) => (
                  <label
                    key={member.userId}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: 8,
                      padding: '8px',
                      cursor: 'pointer',
                      borderRadius: 'var(--radius-sm)',
                      background: selectedSenders.includes(member.userId) 
                        ? 'var(--yellow-800)' 
                        : 'transparent',
                    }}
                  >
                    <input
                      type="checkbox"
                      checked={selectedSenders.includes(member.userId)}
                      onChange={() => toggleSender(member.userId)}
                      style={{ cursor: 'pointer' }}
                    />
                    <span style={{
                      color: 'var(--text)',
                      fontSize: 14,
                      fontWeight: 500,
                    }}>
                      {member.username}
                    </span>
                  </label>
                ))
              ) : (
                <div style={{
                  padding: 12,
                  color: 'var(--text-muted)',
                  textAlign: 'center',
                  fontSize: 14,
                }}>
                  No members available
                </div>
              )}
            </div>
          </div>
        )}

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

        {/* Action Buttons */}
        <div style={{
          display: 'flex',
          gap: 12,
          justifyContent: 'flex-end',
        }}>
          <button
            onClick={onClose}
            disabled={isExporting}
            style={{
              padding: '10px 20px',
              border: '2px solid var(--border)',
              borderRadius: 'var(--radius-sm)',
              background: 'var(--bg)',
              color: 'var(--text)',
              cursor: isExporting ? 'not-allowed' : 'pointer',
              fontWeight: 600,
              fontSize: 14,
            }}
          >
            Cancel
          </button>
          <button
            onClick={handleExport}
            disabled={isExporting}
            style={{
              padding: '10px 20px',
              border: 'none',
              borderRadius: 'var(--radius-sm)',
              background: 'var(--accent)',
              color: '#0d0d0d',
              cursor: isExporting ? 'not-allowed' : 'pointer',
              fontWeight: 700,
              fontSize: 14,
            }}
          >
            {isExporting ? 'Exporting...' : 'Export'}
          </button>
        </div>
      </div>
    </div>
  );
}