package com.chess.dto;

import java.util.List;

/** Full game state pushed to client after every move. */
public record GameStateDto(
        String gameId,
        String[][] board,       // [row][col] = "WQ", "BK", null, etc.
        String currentTurn,     // "WHITE" | "BLACK"
        String status,          // "ONGOING"|"CHECK"|"CHECKMATE"|"STALEMATE"|"DRAW_50_MOVE"
        List<String> moveLog,
        boolean aiEnabled,
        String aiColor,
        String aiDifficulty,
        String errorMessage     // null on success
) {}
