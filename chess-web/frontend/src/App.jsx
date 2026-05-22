import React, { useState, useEffect, useRef } from 'react'
import ChessBoard from './components/ChessBoard'
import { useChessGame } from './components/useChessGame'

const palette = {
  bg:      '#1A1A2E',
  panel:   '#16213E',
  accent:  '#E94560',
  blue:    '#0F3460',
  text:    '#E0E0E0',
  muted:   '#8892A4',
  logBg:   '#0D1B2A',
  logText: '#7EC8E3',
  border:  '#0F3460',
}

export default function App() {
  const {
    gameState, selected, legalMoves, loading, aiThinking,
    newGame, handleSquareClick, makeMove,
  } = useChessGame()

  const [showSetup, setShowSetup]       = useState(true)
  const [aiEnabled, setAiEnabled]       = useState(false)
  const [aiColor, setAiColor]           = useState('BLACK')
  const [aiDifficulty, setAiDifficulty] = useState('MEDIUM')
  const [lastMove, setLastMove]         = useState(null)
  const [showPromo, setShowPromo]       = useState(false)
  const [pendingPromo, setPendingPromo] = useState(null)
  const logRef = useRef(null)

  // Auto-scroll move log
  useEffect(() => {
    if (logRef.current) logRef.current.scrollTop = logRef.current.scrollHeight
  }, [gameState?.moveLog])

  const startGame = () => {
    setShowSetup(false)
    newGame(aiEnabled, aiColor, aiDifficulty)
  }

  const handleSquare = async (r, c) => {
    if (!gameState) return
    // Check if this is a pawn promotion move
    const movingPiece = gameState.board?.[selected?.row]?.[selected?.col]
    const isPromo = movingPiece?.endsWith('P')
        && legalMoves.some(m => m.row === r && m.col === c)
        && (r === 0 || r === 7)

    if (isPromo) {
      setPendingPromo({ toRow: r, toCol: c })
      setShowPromo(true)
      return
    }
    await handleSquareClick(r, c)
  }

  const handlePromotion = async (piece) => {
    setShowPromo(false)
    if (pendingPromo) {
      await makeMove(pendingPromo.toRow, pendingPromo.toCol, piece)
      setPendingPromo(null)
    }
  }

  const statusColor = () => {
    const s = gameState?.status
    if (s === 'CHECKMATE') return palette.accent
    if (s === 'CHECK') return '#FFB347'
    return palette.muted
  }

  const statusText = () => {
    const s = gameState?.status
    const turn = gameState?.currentTurn === 'WHITE' ? 'White' : 'Black'
    if (s === 'CHECKMATE') {
      const winner = gameState.currentTurn === 'WHITE' ? 'Black' : 'White'
      return `✦ Checkmate — ${winner} wins!`
    }
    if (s === 'STALEMATE') return 'Stalemate — Draw'
    if (s === 'DRAW_50_MOVE') return '50-move rule — Draw'
    if (s === 'CHECK') return `⚠ ${turn} is in check!`
    if (aiThinking) return 'AI is thinking…'
    return `${turn} to move`
  }

  // ── Setup screen ───────────────────────────────────────────────────────
  if (showSetup) return (
    <div style={{ ...s.page, alignItems: 'center', justifyContent: 'center' }}>
      <div style={s.setupCard}>
        <h1 style={s.setupTitle}>♟ CHESS</h1>
        <p style={{ color: palette.muted, marginBottom: 24 }}>Configure your game</p>

        <label style={s.label}>Mode</label>
        <div style={s.toggleRow}>
          {['2 Players', 'vs AI'].map(mode => (
            <button key={mode}
              style={{ ...s.toggleBtn, ...(aiEnabled === (mode === 'vs AI') ? s.toggleActive : {}) }}
              onClick={() => setAiEnabled(mode === 'vs AI')}>
              {mode}
            </button>
          ))}
        </div>

        {aiEnabled && <>
          <label style={s.label}>AI plays</label>
          <div style={s.toggleRow}>
            {['WHITE', 'BLACK'].map(c => (
              <button key={c}
                style={{ ...s.toggleBtn, ...(aiColor === c ? s.toggleActive : {}) }}
                onClick={() => setAiColor(c)}>
                {c === 'WHITE' ? '● White' : '● Black'}
              </button>
            ))}
          </div>

          <label style={s.label}>Difficulty</label>
          <div style={s.toggleRow}>
            {['EASY', 'MEDIUM', 'HARD'].map(d => (
              <button key={d}
                style={{ ...s.toggleBtn, ...(aiDifficulty === d ? s.toggleActive : {}) }}
                onClick={() => setAiDifficulty(d)}>
                {d[0] + d.slice(1).toLowerCase()}
              </button>
            ))}
          </div>
        </>}

        <button style={s.startBtn} onClick={startGame}>Start Game →</button>
      </div>
    </div>
  )

  // ── Main game UI ───────────────────────────────────────────────────────
  return (
    <div style={s.page}>
      {/* Title bar */}
      <div style={s.titleBar}>
        <span style={s.titleText}>♟ CHESS</span>
        <div style={{ display: 'flex', gap: 8 }}>
          <button style={s.btn} onClick={() => setShowSetup(true)}>⚙ Settings</button>
          <button style={s.btn} onClick={() => newGame(aiEnabled, aiColor, aiDifficulty)}>New Game</button>
        </div>
      </div>

      <div style={s.body}>
        {/* Board */}
        <div style={s.boardArea}>
          {loading ? (
            <div style={{ color: palette.text }}>Loading…</div>
          ) : gameState ? (
            <ChessBoard
              gameState={gameState}
              selected={selected}
              legalMoves={legalMoves}
              onSquareClick={handleSquare}
              lastMove={lastMove}
            />
          ) : null}
        </div>

        {/* Sidebar */}
        <div style={s.sidebar}>
          {/* Turn box */}
          <div style={s.turnBox}>
            <div style={s.sectionLabel}>TURN</div>
            <div style={{
              ...s.turnWho,
              color: gameState?.status === 'CHECKMATE' ? palette.accent
                   : gameState?.status === 'CHECK' ? '#FFB347'
                   : gameState?.currentTurn === 'WHITE' ? '#F5F5DC' : '#A0A0A0'
            }}>
              {gameState?.status === 'CHECKMATE'
                ? `✦ ${gameState.currentTurn === 'WHITE' ? 'Black' : 'White'} wins`
                : gameState?.currentTurn === 'WHITE' ? '● White' : '● Black'}
            </div>
          </div>

          {/* AI badge */}
          {gameState?.aiEnabled && (
            <div style={s.aiBadge}>
              <div style={{ ...s.sectionLabel, color: palette.logText }}>AI OPPONENT</div>
              <div style={{ color: palette.muted, fontSize: 12, marginTop: 3 }}>
                {gameState.aiColor === 'WHITE' ? 'White' : 'Black'} · {gameState.aiDifficulty}
                {aiThinking && <span style={{ color: '#FFB347' }}> · thinking…</span>}
              </div>
            </div>
          )}

          <div style={s.sectionLabel}>MOVE LOG</div>
          <div ref={logRef} style={s.moveLog}>
            {gameState?.moveLog?.map((line, i) => (
              <div key={i}>{line}</div>
            ))}
          </div>

          <button style={{ ...s.btn, width: '100%', marginTop: 12 }}
            onClick={() => {/* resign */}}>
            Resign
          </button>
        </div>
      </div>

      {/* Status bar */}
      <div style={{ ...s.statusBar, color: statusColor() }}>
        {statusText()}
      </div>

      {/* Promotion dialog */}
      {showPromo && (
        <div style={s.overlay}>
          <div style={s.promoBox}>
            <div style={{ color: palette.text, fontFamily: 'Georgia', fontSize: 14, marginBottom: 14, fontWeight: 'bold' }}>
              Choose Promotion
            </div>
            <div style={{ display: 'flex', gap: 10 }}>
              {[
                { piece: 'QUEEN',  glyph: '♕', label: 'Queen'  },
                { piece: 'ROOK',   glyph: '♖', label: 'Rook'   },
                { piece: 'BISHOP', glyph: '♗', label: 'Bishop' },
                { piece: 'KNIGHT', glyph: '♘', label: 'Knight' },
              ].map(({ piece, glyph, label }) => (
                <button key={piece} style={s.promoBtn} onClick={() => handlePromotion(piece)}>
                  <span style={{ fontSize: 38, lineHeight: 1 }}>{glyph}</span>
                  <span style={{ fontSize: 11, color: palette.muted }}>{label}</span>
                </button>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

// ── Styles ─────────────────────────────────────────────────────────────────
const s = {
  page: {
    display: 'flex', flexDirection: 'column',
    minHeight: '100vh', background: palette.bg,
    fontFamily: 'Georgia, serif',
  },
  titleBar: {
    background: palette.panel,
    borderBottom: `1px solid ${palette.border}`,
    padding: '10px 24px',
    display: 'flex', justifyContent: 'space-between', alignItems: 'center',
  },
  titleText: { color: palette.text, fontSize: 22, fontWeight: 'bold', letterSpacing: 2 },
  body: { display: 'flex', flex: 1, gap: 0 },
  boardArea: {
    flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center',
    padding: '24px 20px 24px 28px',
  },
  sidebar: {
    width: 220, background: palette.panel,
    borderLeft: `1px solid ${palette.border}`,
    padding: 16, display: 'flex', flexDirection: 'column', gap: 10,
  },
  turnBox: {
    background: palette.blue, borderRadius: 6, padding: '10px 12px',
  },
  sectionLabel: {
    fontFamily: "'Courier New', monospace", fontSize: 10, letterSpacing: 1,
    color: palette.muted, textTransform: 'uppercase', marginBottom: 2,
  },
  turnWho: { fontSize: 16, fontWeight: 'bold', fontFamily: 'Georgia' },
  aiBadge: {
    background: '#0A2540', borderRadius: 6, padding: '8px 12px',
  },
  moveLog: {
    flex: 1, background: palette.logBg, borderRadius: 4,
    border: `1px solid ${palette.border}`, padding: 8,
    overflowY: 'auto', fontFamily: "'Courier New', monospace",
    fontSize: 12, color: palette.logText, lineHeight: 1.8,
    minHeight: 200,
  },
  statusBar: {
    background: palette.panel, textAlign: 'center',
    padding: '7px 0', fontSize: 13, fontStyle: 'italic',
    borderTop: `1px solid ${palette.border}`,
  },
  btn: {
    background: palette.blue, color: palette.text,
    border: 'none', padding: '6px 14px', borderRadius: 5,
    fontFamily: 'Georgia, serif', fontSize: 12, cursor: 'pointer',
  },
  // Setup screen
  setupCard: {
    background: palette.panel, borderRadius: 12,
    border: `1px solid ${palette.border}`,
    padding: '36px 40px', minWidth: 340,
    display: 'flex', flexDirection: 'column',
  },
  setupTitle: { color: palette.text, fontSize: 28, margin: '0 0 4px', letterSpacing: 3 },
  label: { color: palette.muted, fontSize: 11, fontFamily: "'Courier New'", letterSpacing: 1, marginBottom: 6 },
  toggleRow: { display: 'flex', gap: 8, marginBottom: 20 },
  toggleBtn: {
    flex: 1, padding: '8px 0', background: palette.blue, color: palette.text,
    border: 'none', borderRadius: 6, fontFamily: 'Georgia', fontSize: 13, cursor: 'pointer',
  },
  toggleActive: { background: palette.accent },
  startBtn: {
    marginTop: 8, padding: '12px 0', background: palette.accent,
    color: '#fff', border: 'none', borderRadius: 6,
    fontFamily: 'Georgia', fontSize: 15, cursor: 'pointer', fontWeight: 'bold',
  },
  // Promotion
  overlay: {
    position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.7)',
    display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 100,
  },
  promoBox: {
    background: palette.panel, borderRadius: 12,
    border: `1.5px solid ${palette.border}`,
    padding: '24px 28px', display: 'flex', flexDirection: 'column', alignItems: 'center',
  },
  promoBtn: {
    background: palette.blue, border: 'none', borderRadius: 8,
    padding: '10px 14px', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4,
    cursor: 'pointer', color: '#FFFAF0', minWidth: 64,
  },
}
