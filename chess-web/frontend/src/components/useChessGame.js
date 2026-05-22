import { useState, useEffect, useCallback, useRef } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const API = '/api/game'

export function useChessGame() {
  const [gameState, setGameState]   = useState(null)   // GameStateDto
  const [selected,  setSelected]    = useState(null)   // {row, col}
  const [legalMoves, setLegalMoves] = useState([])     // [{row,col}]
  const [loading,   setLoading]     = useState(false)
  const [aiThinking, setAiThinking] = useState(false)
  const stompRef = useRef(null)

  // ── WebSocket connection ──────────────────────────────────────────────
  const connectWs = useCallback((gameId) => {
    if (stompRef.current) stompRef.current.deactivate()

    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      onConnect: () => {
        client.subscribe(`/topic/game/${gameId}`, (msg) => {
          const state = JSON.parse(msg.body)
          setGameState(state)
          setAiThinking(false)
          setSelected(null)
          setLegalMoves([])
        })
      },
      reconnectDelay: 2000,
    })
    client.activate()
    stompRef.current = client
  }, [])

  useEffect(() => () => stompRef.current?.deactivate(), [])

  // ── New game ──────────────────────────────────────────────────────────
  const newGame = useCallback(async (aiEnabled = false, aiColor = 'BLACK', aiDifficulty = 'MEDIUM') => {
    setLoading(true)
    setSelected(null)
    setLegalMoves([])
    try {
      const res = await fetch(`${API}/new`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ aiEnabled, aiColor, aiDifficulty })
      })
      const state = await res.json()
      setGameState(state)
      connectWs(state.gameId)
      if (state.aiEnabled && state.aiColor === state.currentTurn) setAiThinking(true)
    } finally {
      setLoading(false)
    }
  }, [connectWs])

  // ── Select piece → fetch legal moves ─────────────────────────────────
  const selectPiece = useCallback(async (row, col) => {
    if (!gameState) return
    if (selected?.row === row && selected?.col === col) {
      setSelected(null); setLegalMoves([]); return
    }
    try {
      const res = await fetch(`${API}/${gameState.gameId}/legal-moves?row=${row}&col=${col}`)
      const data = await res.json()
      if (data.moves.length > 0) {
        setSelected({ row, col })
        setLegalMoves(data.moves.map(([r, c]) => ({ row: r, col: c })))
      } else {
        setSelected(null); setLegalMoves([])
      }
    } catch { setSelected(null); setLegalMoves([]) }
  }, [gameState, selected])

  // ── Make a move ───────────────────────────────────────────────────────
  const makeMove = useCallback(async (toRow, toCol, promotionPiece = 'QUEEN') => {
    if (!gameState || !selected) return
    setAiThinking(false)

    const body = {
      gameId: gameState.gameId,
      fromRow: selected.row, fromCol: selected.col,
      toRow, toCol, promotionPiece
    }

    setSelected(null); setLegalMoves([])

    const res = await fetch(`${API}/move`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    })
    const state = await res.json()
    if (state.errorMessage) return  // illegal move — no state change
    setGameState(state)

    const aiTurn = state.aiEnabled && state.aiColor === state.currentTurn && !state.status?.includes('CHECKMATE') && state.status !== 'STALEMATE'
    if (aiTurn) setAiThinking(true)
  }, [gameState, selected])

  // ── Click handler ─────────────────────────────────────────────────────
  const handleSquareClick = useCallback(async (row, col) => {
    if (!gameState || gameState.status === 'CHECKMATE' || gameState.status === 'STALEMATE') return
    if (aiThinking) return

    const piece = gameState.board?.[row]?.[col]
    const isLegal = legalMoves.some(m => m.row === row && m.col === col)

    if (isLegal) {
      // Check if promotion needed
      const movingPiece = gameState.board?.[selected.row]?.[selected.col]
      const isPromo = movingPiece?.endsWith('P') && (row === 0 || row === 7)
      // promotionPiece choice handled in UI — default QUEEN here
      await makeMove(row, col, 'QUEEN')
    } else if (piece && piece[0] === gameState.currentTurn[0]) {
      await selectPiece(row, col)
    } else {
      setSelected(null); setLegalMoves([])
    }
  }, [gameState, legalMoves, selected, aiThinking, makeMove, selectPiece])

  return {
    gameState, selected, legalMoves,
    loading, aiThinking,
    newGame, handleSquareClick, makeMove, selectPiece
  }
}
