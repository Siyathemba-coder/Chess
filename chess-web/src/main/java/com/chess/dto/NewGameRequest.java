package com.chess.dto;

/** Request body for POST /api/game/new */
public record NewGameRequest(
        boolean aiEnabled,
        String  aiColor,       // "WHITE" | "BLACK"
        String  aiDifficulty   // "EASY" | "MEDIUM" | "HARD"
) {}
