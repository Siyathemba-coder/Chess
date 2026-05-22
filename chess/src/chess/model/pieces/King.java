package chess.model.pieces;

import chess.model.Board;
import chess.model.Piece;
import java.util.ArrayList;
import java.util.List;

public class King extends Piece {

    private static final int[][] DIRS = {
        {-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}
    };

    public King(Color color) { super(color, Type.KING); }

    @Override
    public List<int[]> getCandidateMoves(int row, int col, Board board) {
        List<int[]> moves = new ArrayList<>();

        for (int[] d : DIRS) {
            int r = row + d[0], c = col + d[1];
            if (board.inBounds(r, c)) {
                Piece target = board.get(r, c);
                if (target == null || isOpponent(target))
                    moves.add(new int[]{r, c});
            }
        }

        // Castling (hasMoved check only — check/path safety handled by MoveValidator)
        if (!hasMoved) {
            // Kingside
            Piece kRook = board.get(row, 7);
            if (kRook != null && !kRook.hasMoved()
                    && board.isEmpty(row, 5) && board.isEmpty(row, 6))
                moves.add(new int[]{row, 6, 1}); // flag=1 → kingside

            // Queenside
            Piece qRook = board.get(row, 0);
            if (qRook != null && !qRook.hasMoved()
                    && board.isEmpty(row, 1) && board.isEmpty(row, 2) && board.isEmpty(row, 3))
                moves.add(new int[]{row, 2, 2}); // flag=2 → queenside
        }

        return moves;
    }
}
