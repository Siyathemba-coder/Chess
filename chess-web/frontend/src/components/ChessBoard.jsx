import React, { useState } from 'react'

const GLYPHS = {
  WK:'♔', WQ:'♕', WR:'♖', WB:'♗', WN:'♘', WP:'♙',
  BK:'♚', BQ:'♛', BR:'♜', BB:'♝', BN:'♞', BP:'♟',
}

const FILES = ['a','b','c','d','e','f','g','h']
const RANKS = ['8','7','6','5','4','3','2','1']

export default function ChessBoard({ gameState, selected, legalMoves, onSquareClick, lastMove }) {
  if (!gameState?.board) return null

  const isSelected  = (r, c) => selected?.row === r && selected?.col === c
  const isLegal     = (r, c) => legalMoves.some(m => m.row === r && m.col === c)
  const isLastMove  = (r, c) => lastMove && (
    (lastMove.fromRow === r && lastMove.fromCol === c) ||
    (lastMove.toRow   === r && lastMove.toCol   === c)
  )
  const isInCheck   = (r, c) => {
    if (gameState.status !== 'CHECK' && gameState.status !== 'CHECKMATE') return false
    const piece = gameState.board[r][c]
    return piece && piece[1] === 'K' && piece[0] === gameState.currentTurn[0]
  }

  return (
    <div style={styles.container}>
      {/* File labels top */}
      <div style={styles.fileRow}>
        <div style={styles.rankGutter} />
        {FILES.map(f => <div key={f} style={styles.coordLabel}>{f}</div>)}
        <div style={styles.rankGutter} />
      </div>

      {/* Board rows */}
      {gameState.board.map((row, r) => (
        <div key={r} style={styles.row}>
          <div style={{...styles.coordLabel, ...styles.rankLabel}}>{RANKS[r]}</div>

          {row.map((piece, c) => {
            const light = (r + c) % 2 === 0
            const sel   = isSelected(r, c)
            const legal = isLegal(r, c)
            const last  = isLastMove(r, c)
            const check = isInCheck(r, c)

            let bg = light ? '#F0D9B5' : '#B58863'
            if (last)  bg = light ? '#CDD26A' : '#AABA2E'
            if (sel)   bg = '#F6F669'
            if (check) bg = '#E94560'

            return (
              <div
                key={c}
                onClick={() => onSquareClick(r, c)}
                style={{ ...styles.square, background: bg, cursor: 'pointer' }}
              >
                {/* Legal move indicator */}
                {legal && !piece && (
                  <div style={styles.moveDot} />
                )}
                {legal && piece && (
                  <div style={styles.captureRing} />
                )}

                {/* Piece glyph */}
                {piece && (
                  <span style={{
                    ...styles.piece,
                    color: piece[0] === 'W' ? '#FFFAF0' : '#1A1A2E',
                    textShadow: piece[0] === 'W'
                      ? '0 0 2px #3A2A1A, 0 0 2px #3A2A1A, 1px 2px 3px rgba(0,0,0,0.4)'
                      : '1px 2px 3px rgba(0,0,0,0.5)',
                  }}>
                    {GLYPHS[piece] || piece}
                  </span>
                )}
              </div>
            )
          })}

          <div style={{...styles.coordLabel, ...styles.rankLabel}}>{RANKS[r]}</div>
        </div>
      ))}

      {/* File labels bottom */}
      <div style={styles.fileRow}>
        <div style={styles.rankGutter} />
        {FILES.map(f => <div key={f} style={styles.coordLabel}>{f}</div>)}
        <div style={styles.rankGutter} />
      </div>
    </div>
  )
}

const SQ = 72

const styles = {
  container: {
    display: 'inline-block',
    border: '3px solid #6B3F1E',
    borderRadius: 6,
    overflow: 'hidden',
    boxShadow: '0 8px 32px rgba(0,0,0,0.5)',
    userSelect: 'none',
  },
  fileRow: {
    display: 'flex',
    background: '#4A2A0A',
  },
  row: {
    display: 'flex',
  },
  coordLabel: {
    width: SQ,
    height: 18,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: 11,
    fontFamily: 'Georgia, serif',
    fontWeight: 'bold',
    color: '#C8A882',
    background: '#4A2A0A',
  },
  rankGutter: {
    width: 18,
    height: 18,
    background: '#4A2A0A',
  },
  rankLabel: {
    width: 18,
    height: SQ,
  },
  square: {
    width: SQ,
    height: SQ,
    position: 'relative',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    transition: 'background 0.1s',
  },
  piece: {
    fontSize: 52,
    lineHeight: 1,
    position: 'relative',
    zIndex: 2,
  },
  moveDot: {
    position: 'absolute',
    width: 24,
    height: 24,
    borderRadius: '50%',
    background: 'rgba(20,20,20,0.22)',
    zIndex: 1,
  },
  captureRing: {
    position: 'absolute',
    inset: 3,
    borderRadius: '50%',
    border: '4px solid rgba(20,20,20,0.22)',
    zIndex: 1,
  },
}
