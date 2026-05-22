package chess.model;

public class Move {

    public enum Flag { NORMAL, CASTLE_KINGSIDE, CASTLE_QUEENSIDE, EN_PASSANT, PROMOTION }

    public final int fromRow, fromCol;
    public final int toRow,   toCol;
    public final Flag flag;
    public final Piece.Type promotionPiece; // used when flag == PROMOTION

    public Move(int fromRow, int fromCol, int toRow, int toCol) {
        this(fromRow, fromCol, toRow, toCol, Flag.NORMAL, null);
    }

    public Move(int fromRow, int fromCol, int toRow, int toCol, Flag flag) {
        this(fromRow, fromCol, toRow, toCol, flag, null);
    }

    public Move(int fromRow, int fromCol, int toRow, int toCol,
                Flag flag, Piece.Type promotionPiece) {
        this.fromRow        = fromRow;
        this.fromCol        = fromCol;
        this.toRow          = toRow;
        this.toCol          = toCol;
        this.flag           = flag;
        this.promotionPiece = promotionPiece;
    }

    @Override
    public String toString() {
        char[] cols = {'a','b','c','d','e','f','g','h'};
        return "" + cols[fromCol] + (8 - fromRow) + cols[toCol] + (8 - toRow)
                + (flag == Flag.PROMOTION ? "=" + promotionPiece : "");
    }
}
