import React, { useEffect, useMemo, useState } from 'react'

function badge(sentiment) {
  if (sentiment === 'BULLISH') return '🟢 Bullish'
  if (sentiment === 'BEARISH') return '🔴 Bearish'
  return '🟡 Neutral'
}

export default function App() {
  const [events, setEvents] = useState([])
  const [connected, setConnected] = useState(false)
  const [streamActive, setStreamActive] = useState(true)

  useEffect(() => {
    if (!streamActive) return

    const es = new EventSource(`/api/stream?t=${Date.now()}`)
    es.onopen = () => setConnected(true)
    es.onerror = () => setConnected(false)
    es.onmessage = (msg) => {
      try {
        const data = JSON.parse(msg.data)
        setEvents((prev) => {
          // Deduplicate based on URL (or headline if local ID isn't unique enough across runs)
          if (prev.some((e) => e.url === data.url)) {
            return prev
          }
          return [data, ...prev].slice(0, 50)
        })
      } catch (e) {
        // ignore
      }
    }
    return () => {
      es.close()
      setConnected(false)
    }
  }, [streamActive])

  const header = useMemo(() => connected ? 'Connected ✅' : 'Disconnected ❌', [connected])

  const startDemo = async () => {
    setStreamActive(true)
    await fetch('/api/feed/start', { method: 'POST' })
  }

  const stopDemo = async () => {
    setStreamActive(false)
    await fetch('/api/feed/stop', { method: 'POST' })
  }

  return (
    <div style={{ fontFamily: 'system-ui, sans-serif', padding: 24, maxWidth: 980, margin: '0 auto' }}>
      <h1>LiveStock AI</h1>
      <p style={{ marginTop: -8 }}>{header} — streaming SSE updates from the backend.</p>

      <div style={{ display: 'flex', gap: 12, marginBottom: 16 }}>
        <button onClick={startDemo}>Start Feed</button>
        <button onClick={stopDemo}>Stop Feed</button>
      </div>

      <div style={{ display: 'grid', gap: 12 }}>
        {events.map((e) => (
          <div key={e.id} style={{ border: '1px solid #ddd', borderRadius: 12, padding: 12 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', gap: 12 }}>
              <strong>{e.headline}</strong>
              <span>{badge(e.sentiment)} · Risk {e.riskScore}/10</span>
            </div>
            <div style={{ color: '#555', marginTop: 4 }}>
              {e.source} · {new Date(e.timestamp).toLocaleString()}
            </div>
            <div style={{ marginTop: 8 }}>{e.summary}</div>
            {e.url ? (
              <div style={{ marginTop: 8 }}>
                <a href={e.url} target="_blank" rel="noreferrer">{e.url}</a>
              </div>
            ) : null}
          </div>
        ))}
        {events.length === 0 ? (
          <div style={{ color: '#666' }}>
            No events yet. Click “Start demo feed” (and run backend), or POST to <code>/api/news</code>.
          </div>
        ) : null}
      </div>
    </div>
  )
}
