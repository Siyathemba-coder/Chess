package chess.engine;

import chess.model.*;

import java.util.List;

/**
 * Orchestrates game flow: validates turns, applies moves,
 * updates GameState (check / checkmate / stalemate / 50-move draw).
 */
public class GameEngine {

    private final Board         board;
    private final GameState     state;
    private final MoveValidator validator;

    public GameEngine() {
        this.board     = new Board();
        this.state     = new GameState();
        this.validator = new MoveValidator();
    }

    // Public API

    public Board     getBoard()     { return board; }
    public GameState getState()     { return state; }

    /**
     * Attempt to make a move. Returns true if the move was legal and applied.
     * For pawn promotions, defaults to QUEEN.
     */
    public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        return makeMove(fromRow, fromCol, toRow, toCol, Piece.Type.QUEEN);
    }

    /**
     * Attempt to make a move with an explicit promotion piece type.
     * promotionType is only used when the move is a pawn promotion.
     */
    public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol,
                            Piece.Type promotionType) {
        if (state.isOver()) return false;

        Piece piece = board.get(fromRow, fromCol);
        if (piece == null || piece.getColor() != state.getCurrentTurn()) return false;

        List<Move> legal = validator.getLegalMoves(fromRow, fromCol, board);
        Move chosen = legal.stream()
                .filter(m -> m.toRow == toRow && m.toCol == toCol)
                .findFirst().orElse(null);

        if (chosen == null) return false;

        // Override promotion piece if specified
        if (chosen.flag == Move.Flag.PROMOTION && promotionType != null) {
            chosen = new Move(fromRow, fromCol, toRow, toCol,
                              Move.Flag.PROMOTION, promotionType);
        }

        boolean wasCapture = board.get(toRow, toCol) != null
                || chosen.flag == Move.Flag.EN_PASSANT;
        boolean wasPawnMove = piece.getType() == Piece.Type.PAWN;

        board.applyMove(chosen);
        state.recordMove(chosen, wasCapture, wasPawnMove);
        state.switchTurn();
        updateStatus();

        return true;
    }

    /** Returns true if the move at (from→to) is a pawn promotion. */
    public boolean isPromotion(int fromRow, int fromCol, int toRow, int toCol) {
        Piece p = board.get(fromRow, fromCol);
        return p != null && p.getType() == Piece.Type.PAWN
                && (toRow == 0 || toRow == 7);
    }

    /** Returns all legal moves for the piece at (row, col). */
    public List<Move> getLegalMovesFor(int row, int col) {
        return validator.getLegalMoves(row, col, board);
    }

    // Status

    private void updateStatus() {
        Piece.Color turn = state.getCurrentTurn();

        if (state.isFiftyMoveRule()) {
            state.setStatus(GameState.Status.DRAW_50_MOVE);
            return;
        }

        boolean inCheck    = validator.isInCheck(turn, board);
        boolean hasMoves   = !validator.getAllLegalMoves(turn, board).isEmpty();

        if (!hasMoves) {
            state.setStatus(inCheck
                    ? GameState.Status.CHECKMATE
                    : GameState.Status.STALEMATE);
        } else {
            state.setStatus(inCheck
                    ? GameState.Status.CHECK
                    : GameState.Status.ONGOING);
        }
    }
}
