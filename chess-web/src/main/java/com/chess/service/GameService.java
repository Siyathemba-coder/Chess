package com.chess.service;

import chess.engine.ChessAI;
import chess.engine.GameEngine;
import chess.model.*;
import com.chess.dto.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

/**
 * Manages all in-memory game sessions.
 * Each game has a unique ID, its own GameEngine, optional ChessAI,
 * and a move log.
 */
@Service
public class GameService {

    private final SimpMessagingTemplate   messaging;
    private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();

    public GameService(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    // ── Session inner class ────────────────────────────────────────────────

    private static class GameSession {
        final String      id;
        final GameEngine  engine;
        ChessAI           ai;
        boolean           aiEnabled;
        Piece.Color       aiColor;
        final List<String> moveLog = new ArrayList<>();
        int               moveNumber = 1;

        GameSession(String id, boolean aiEnabled, Piece.Color aiColor, ChessAI ai) {
            this.id        = id;
            this.engine    = new GameEngine();
            this.aiEnabled = aiEnabled;
            this.aiColor   = aiColor;
            this.ai        = ai;
        }
    }

    // ── Public API ─────────────────────────────────────────────────────────

    /** Create a new game, return its initial state. */
    public GameStateDto newGame(NewGameRequest req) {
        String id = UUID.randomUUID().toString().substring(0, 8);

        boolean ai       = req.aiEnabled();
        Piece.Color color = "WHITE".equals(req.aiColor())
                ? Piece.Color.WHITE : Piece.Color.BLACK;
        ChessAI engine   = ai ? new ChessAI(parseDifficulty(req.aiDifficulty())) : null;

        GameSession session = new GameSession(id, ai, color, engine);
        sessions.put(id, session);

        GameStateDto state = toDto(session, null);

        // If AI plays White, trigger first move
        if (ai && color == Piece.Color.WHITE) {
            CompletableFuture.runAsync(() -> runAiMove(session));
        }

        return state;
    }

    /** Get current state for a game. */
    public Optional<GameStateDto> getGame(String id) {
        GameSession s = sessions.get(id);
        return s == null ? Optional.empty() : Optional.of(toDto(s, null));
    }

    /** Apply a player move; run AI response if needed. Returns updated state. */
    public GameStateDto applyMove(MoveRequest req) {
        GameSession s = sessions.get(req.gameId());
        if (s == null) return error(req.gameId(), "Game not found");
        if (s.engine.getState().isOver()) return toDto(s, "Game is already over");

        Piece.Type promo = parsePromotion(req.promotionPiece());
        boolean ok = s.engine.makeMove(req.fromRow(), req.fromCol(), req.toRow(), req.toCol(), promo);
        if (!ok) return toDto(s, "Illegal move");

        recordMove(s, req.fromRow(), req.fromCol(), req.toRow(), req.toCol());

        GameStateDto dto = toDto(s, null);
        broadcast(s.id, dto);

        // Trigger AI asynchronously
        if (!s.engine.getState().isOver() && s.aiEnabled
                && s.engine.getState().getCurrentTurn() == s.aiColor) {
            CompletableFuture.runAsync(() -> runAiMove(s));
        }

        return dto;
    }

    /** Get legal moves for a piece. */
    public LegalMovesDto legalMoves(String gameId, int row, int col) {
        GameSession s = sessions.get(gameId);
        if (s == null) return new LegalMovesDto(List.of());
        List<Move> moves = s.engine.getLegalMovesFor(row, col);
        List<int[]> targets = moves.stream().map(m -> new int[]{m.toRow, m.toCol}).toList();
        return new LegalMovesDto(targets);
    }

    // ── AI ─────────────────────────────────────────────────────────────────

    private void runAiMove(GameSession s) {
        if (s.ai == null || !s.aiEnabled || s.engine.getState().isOver()) return;
        if (s.engine.getState().getCurrentTurn() != s.aiColor) return;

        Move m = s.ai.getBestMove(s.engine.getBoard(), s.aiColor);
        if (m == null) return;

        synchronized (s) {
            s.engine.makeMove(m.fromRow, m.fromCol, m.toRow, m.toCol,
                    m.flag == Move.Flag.PROMOTION ? m.promotionPiece : Piece.Type.QUEEN);
            recordMove(s, m.fromRow, m.fromCol, m.toRow, m.toCol);
        }

        GameStateDto dto = toDto(s, null);
        broadcast(s.id, dto);
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private void broadcast(String gameId, GameStateDto dto) {
        messaging.convertAndSend("/topic/game/" + gameId, dto);
    }

    private void recordMove(GameSession s, int fr, int fc, int tr, int tc) {
        Piece p = s.engine.getBoard().get(tr, tc);
        if (p == null) return; // already moved; approximate from indices

        String cols = "abcdefgh";
        String note = cols.charAt(fc) + String.valueOf(8 - tr);
        boolean isWhiteTurn = s.engine.getState().getCurrentTurn() == Piece.Color.BLACK; // just switched
        if (isWhiteTurn) {
            s.moveLog.add(s.moveNumber + ". " + note);
        } else {
            if (!s.moveLog.isEmpty()) {
                String last = s.moveLog.get(s.moveLog.size() - 1);
                s.moveLog.set(s.moveLog.size() - 1, last + "  " + note);
            } else {
                s.moveLog.add(note);
            }
            s.moveNumber++;
        }
    }

    private GameStateDto toDto(GameSession s, String error) {
        Board board = s.engine.getBoard();
        String[][] grid = new String[8][8];
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                Piece p = board.get(r, c);
                grid[r][c] = p == null ? null
                        : (p.getColor() == Piece.Color.WHITE ? "W" : "B") + p.getType().name().charAt(0);
            }
        return new GameStateDto(
                s.id, grid,
                s.engine.getState().getCurrentTurn().name(),
                s.engine.getState().getStatus().name(),
                new ArrayList<>(s.moveLog),
                s.aiEnabled,
                s.aiColor == null ? null : s.aiColor.name(),
                s.ai == null ? null : s.ai.getDifficulty().name(),
                error
        );
    }

    private GameStateDto error(String id, String msg) {
        return new GameStateDto(id, null, null, null, List.of(), false, null, null, msg);
    }

    private ChessAI.Difficulty parseDifficulty(String d) {
        if (d == null) return ChessAI.Difficulty.MEDIUM;
        return switch (d.toUpperCase()) {
            case "EASY" -> ChessAI.Difficulty.EASY;
            case "HARD" -> ChessAI.Difficulty.HARD;
            default     -> ChessAI.Difficulty.MEDIUM;
        };
    }

    private Piece.Type parsePromotion(String s) {
        if (s == null) return Piece.Type.QUEEN;
        return switch (s.toUpperCase()) {
            case "ROOK"   -> Piece.Type.ROOK;
            case "BISHOP" -> Piece.Type.BISHOP;
            case "KNIGHT" -> Piece.Type.KNIGHT;
            default       -> Piece.Type.QUEEN;
        };
    }
}
