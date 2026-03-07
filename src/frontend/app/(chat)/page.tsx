export default function ChatHome() {
  return (
    <div
      className="dot-bg"
      style={{
        flex: 1,
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 12,
      }}
    >
      <div
        style={{
          fontSize: 64,
          fontFamily: 'Press Start 2P, monospace',
          color: 'var(--accent)',
          lineHeight: 1,
        }}
      >
        vibes
      </div>
      <p
        style={{
          color: 'var(--text-muted)',
          fontSize: 16,
          fontWeight: 600,
          margin: 0,
        }}
      >
        Select a chat or hit + to start a new one
      </p>
    </div>
  );
}
