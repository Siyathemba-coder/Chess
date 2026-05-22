package com.chess.dto;

import java.util.List;

/** Sent by client to make a move. */
public record MoveRequest(
        String gameId,
        int fromRow, int fromCol,
        int toRow,   int toCol,
        String promotionPiece   // "QUEEN"|"ROOK"|"BISHOP"|"KNIGHT", nullable
) {}
