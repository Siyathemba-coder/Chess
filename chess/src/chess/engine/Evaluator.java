package chess.engine;

import chess.model.Piece;

/**
 * Static piece values and piece-square tables (PSTs) for positional evaluation.
 * PSTs are from White's perspective (row 0 = rank 8, row 7 = rank 1).
 * For Black, the table is mirrored vertically.
 */
public final class Evaluator {

    private Evaluator() {}

    // Material values (centipawns)
    public static final int PAWN_VAL   = 100;
    public static final int KNIGHT_VAL = 320;
    public static final int BISHOP_VAL = 330;
    public static final int ROOK_VAL   = 500;
    public static final int QUEEN_VAL  = 900;
    public static final int KING_VAL   = 20_000;
    public static final int CHECKMATE  = 100_000;

    public static int material(Piece.Type type) {
        return switch (type) {
            case PAWN   -> PAWN_VAL;
            case KNIGHT -> KNIGHT_VAL;
            case BISHOP -> BISHOP_VAL;
            case ROOK   -> ROOK_VAL;
            case QUEEN  -> QUEEN_VAL;
            case KING   -> KING_VAL;
        };
    }

    // Piece-square tables (White's perspective, row 0 = back rank)

    private static final int[] PST_PAWN = {
         0,  0,  0,  0,  0,  0,  0,  0,
        50, 50, 50, 50, 50, 50, 50, 50,
        10, 10, 20, 30, 30, 20, 10, 10,
         5,  5, 10, 25, 25, 10,  5,  5,
         0,  0,  0, 20, 20,  0,  0,  0,
         5, -5,-10,  0,  0,-10, -5,  5,
         5, 10, 10,-20,-20, 10, 10,  5,
         0,  0,  0,  0,  0,  0,  0,  0
    };

    private static final int[] PST_KNIGHT = {
        -50,-40,-30,-30,-30,-30,-40,-50,
        -40,-20,  0,  0,  0,  0,-20,-40,
        -30,  0, 10, 15, 15, 10,  0,-30,
        -30,  5, 15, 20, 20, 15,  5,-30,
        -30,  0, 15, 20, 20, 15,  0,-30,
        -30,  5, 10, 15, 15, 10,  5,-30,
        -40,-20,  0,  5,  5,  0,-20,-40,
        -50,-40,-30,-30,-30,-30,-40,-50
    };

    private static final int[] PST_BISHOP = {
        -20,-10,-10,-10,-10,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5, 10, 10,  5,  0,-10,
        -10,  5,  5, 10, 10,  5,  5,-10,
        -10,  0, 10, 10, 10, 10,  0,-10,
        -10, 10, 10, 10, 10, 10, 10,-10,
        -10,  5,  0,  0,  0,  0,  5,-10,
        -20,-10,-10,-10,-10,-10,-10,-20
    };

    private static final int[] PST_ROOK = {
         0,  0,  0,  0,  0,  0,  0,  0,
         5, 10, 10, 10, 10, 10, 10,  5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
         0,  0,  0,  5,  5,  0,  0,  0
    };

    private static final int[] PST_QUEEN = {
        -20,-10,-10, -5, -5,-10,-10,-20,
        -10,  0,  0,  0,  0,  0,  0,-10,
        -10,  0,  5,  5,  5,  5,  0,-10,
         -5,  0,  5,  5,  5,  5,  0, -5,
          0,  0,  5,  5,  5,  5,  0, -5,
        -10,  5,  5,  5,  5,  5,  0,-10,
        -10,  0,  5,  0,  0,  0,  0,-10,
        -20,-10,-10, -5, -5,-10,-10,-20
    };

    private static final int[] PST_KING_MG = {
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -30,-40,-40,-50,-50,-40,-40,-30,
        -20,-30,-30,-40,-40,-30,-30,-20,
        -10,-20,-20,-20,-20,-20,-20,-10,
         20, 20,  0,  0,  0,  0, 20, 20,
         20, 30, 10,  0,  0, 10, 30, 20
    };

    /**
     * Positional bonus for a piece at (row, col), from the piece's color perspective.
     * White uses the table as-is (row 7 = rank 1 = home).
     * Black mirrors vertically.
     */
    public static int pst(Piece.Type type, Piece.Color color, int row, int col) {
        int idx = (color == Piece.Color.WHITE) ? row * 8 + col : (7 - row) * 8 + col;
        int[] table = switch (type) {
            case PAWN   -> PST_PAWN;
            case KNIGHT -> PST_KNIGHT;
            case BISHOP -> PST_BISHOP;
            case ROOK   -> PST_ROOK;
            case QUEEN  -> PST_QUEEN;
            case KING   -> PST_KING_MG;
        };
        return table[idx];
    }
}
