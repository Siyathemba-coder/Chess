package chess.model.pieces;

import chess.model.Board;
import chess.model.Piece;
import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {

    private static final int[][] JUMPS = {
        {-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}
    };

    public Knight(Color color) { super(color, Type.KNIGHT); }

    @Override
    public List<int[]> getCandidateMoves(int row, int col, Board board) {
        List<int[]> moves = new ArrayList<>();
        for (int[] j : JUMPS) {
            int r = row + j[0], c = col + j[1];
            if (board.inBounds(r, c)) {
                Piece target = board.get(r, c);
                if (target == null || isOpponent(target))
                    moves.add(new int[]{r, c});
            }
        }
        return moves;
    }
}
