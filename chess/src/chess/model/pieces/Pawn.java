package chess.model.pieces;

import chess.model.Board;
import chess.model.Piece;
import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {

    public Pawn(Color color) { super(color, Type.PAWN); }

    @Override
    public List<int[]> getCandidateMoves(int row, int col, Board board) {
        List<int[]> moves = new ArrayList<>();
        int dir      = (color == Color.WHITE) ? -1 : 1; // WHITE moves up (row--)
        int startRow = (color == Color.WHITE) ?  6 : 1;

        // Single push
        if (board.isEmpty(row + dir, col))
            moves.add(new int[]{row + dir, col});

        // Double push from starting rank
        if (row == startRow
                && board.isEmpty(row + dir, col)
                && board.isEmpty(row + 2 * dir, col))
            moves.add(new int[]{row + 2 * dir, col});

        // Diagonal captures
        for (int dc : new int[]{-1, 1}) {
            int r = row + dir, c = col + dc;
            if (board.inBounds(r, c)) {
                Piece target = board.get(r, c);
                if (target != null && isOpponent(target))
                    moves.add(new int[]{r, c});
            }
        }

        // En-passant
        int epRow = board.getEnPassantRow();
        int epCol = board.getEnPassantCol();
        if (epRow != -1 && row + dir == epRow && Math.abs(col - epCol) == 1)
            moves.add(new int[]{epRow, epCol, 3}); // flag=3 → en-passant

        return moves;
    }
}
