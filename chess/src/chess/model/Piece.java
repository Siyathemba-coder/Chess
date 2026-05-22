package chess.model;

import java.util.List;

public abstract class Piece {

    public enum Color { WHITE, BLACK }
    public enum Type   { KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN }

    protected Color color;
    protected Type  type;
    protected boolean hasMoved = false;

    public Piece(Color color, Type type) {
        this.color = color;
        this.type  = type;
    }

    /** Returns all pseudo-legal destination squares (does not filter for check). */
    public abstract List<int[]> getCandidateMoves(int row, int col, Board board);

    public Color   getColor()    { return color; }
    public Type    getType()     { return type; }
    public boolean hasMoved()    { return hasMoved; }
    public void    setMoved()    { hasMoved = true; }

    public boolean isOpponent(Piece other) {
        return other != null && other.color != this.color;
    }

    @Override
    public String toString() {
        return (color == Color.WHITE ? "W" : "B") + type.name().charAt(0);
    }
}
