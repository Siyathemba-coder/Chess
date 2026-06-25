package chess.engine;

import chess.model.*;
import chess.model.pieces.Queen;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts a piece's candidate squares into fully legal Move objects,
 * filtering out moves that leave own king in check and validating castling paths.
 */
public class MoveValidator {

    /**
     * Returns all legal moves for the piece at (row, col) on the given board.
     */
    public List<Move> getLegalMoves(int row, int col, Board board) {
        Piece piece = board.get(row, col);
        if (piece == null) return List.of();

        List<int[]> candidates = piece.getCandidateMoves(row, col, board);
        List<Move>  legal      = new ArrayList<>();

        for (int[] sq : candidates) {
            int toRow = sq[0], toCol = sq[1];
            int flag  = sq.length > 2 ? sq[2] : 0;

            Move move = buildMove(row, col, toRow, toCol, flag, piece, board);
            if (move == null) continue;

            // Castling: also verify the king doesn't pass through or land on attacked square
            if (move.flag == Move.Flag.CASTLE_KINGSIDE || move.flag == Move.Flag.CASTLE_QUEENSIDE) {
                if (!castlingPathSafe(move, piece.getColor(), board)) continue;
            }

            // Simulate and verify king is not in check
            Board copy = new Board(board);
            copy.applyMove(move);
            if (!isInCheck(piece.getColor(), copy))
                legal.add(move);
        }

        return legal;
    }

    /**
     * Returns all legal moves for every piece of the given color.
     */
    public List<Move> getAllLegalMoves(Piece.Color color, Board board) {
        List<Move> all = new ArrayList<>();
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                Piece p = board.get(r, c);
                if (p != null && p.getColor() == color)
                    all.addAll(getLegalMoves(r, c, board));
            }
        return all;
    }

    /** Returns true if the given color's king is currently in check. */
    public boolean isInCheck(Piece.Color color, Board board) {
        int[] kingPos = board.findKing(color);
        return isSquareAttacked(kingPos[0], kingPos[1], color, board);
    }

    /** Returns true if (row, col) is attacked by any opponent piece. */
    public boolean isSquareAttacked(int row, int col, Piece.Color defendingColor, Board board) {
        Piece.Color attacker = (defendingColor == Piece.Color.WHITE)
                ? Piece.Color.BLACK : Piece.Color.WHITE;

        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                Piece p = board.get(r, c);
                if (p == null || p.getColor() != attacker) continue;
                for (int[] sq : p.getCandidateMoves(r, c, board)) {
                    if (sq[0] == row && sq[1] == col) return true;
                }
            }
        return false;
    }

    // Helpers

    private Move buildMove(int fromRow, int fromCol, int toRow, int toCol,
                           int flag, Piece piece, Board board) {
        return switch (flag) {
            case 1 -> new Move(fromRow, fromCol, toRow, toCol, Move.Flag.CASTLE_KINGSIDE);
            case 2 -> new Move(fromRow, fromCol, toRow, toCol, Move.Flag.CASTLE_QUEENSIDE);
            case 3 -> new Move(fromRow, fromCol, toRow, toCol, Move.Flag.EN_PASSANT);
            default -> {
                // Pawn promotion?
                if (piece.getType() == Piece.Type.PAWN
                        && (toRow == 0 || toRow == 7)) {
                    // Default to queen promotion; GUI/engine can offer choice
                    yield new Move(fromRow, fromCol, toRow, toCol,
                            Move.Flag.PROMOTION, Piece.Type.QUEEN);
                }
                yield new Move(fromRow, fromCol, toRow, toCol);
            }
        };
    }

    private boolean castlingPathSafe(Move move, Piece.Color color, Board board) {
        int row = move.fromRow;
        // King must not be in check currently
        if (isInCheck(color, board)) return false;

        if (move.flag == Move.Flag.CASTLE_KINGSIDE) {
            return !isSquareAttacked(row, 5, color, board)
                && !isSquareAttacked(row, 6, color, board);
        } else {
            return !isSquareAttacked(row, 3, color, board)
                && !isSquareAttacked(row, 2, color, board);
        }
    }
}
