package chess.model.pieces;

import chess.model.Board;
import chess.model.Piece;
import java.util.ArrayList;
import java.util.List;

public class Bishop extends Piece {

    private static final int[][] DIRS = {{-1,-1},{-1,1},{1,-1},{1,1}};

    public Bishop(Color color) { super(color, Type.BISHOP); }

    @Override
    public List<int[]> getCandidateMoves(int row, int col, Board board) {
        List<int[]> moves = new ArrayList<>();
        for (int[] d : DIRS) {
            int r = row + d[0], c = col + d[1];
            while (board.inBounds(r, c)) {
                Piece target = board.get(r, c);
                if (target == null) {
                    moves.add(new int[]{r, c});
                } else {
                    if (isOpponent(target)) moves.add(new int[]{r, c});
                    break;
                }
                r += d[0]; c += d[1];
            }
        }
        return moves;
    }
}
