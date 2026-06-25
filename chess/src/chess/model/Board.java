package chess.model;

import chess.model.pieces.*;

public class Board {

    private final Piece[][] grid = new Piece[8][8];

    // En-passant target square (-1 if none)
    private int enPassantRow = -1;
    private int enPassantCol = -1;

    public Board() {
        init();
    }

    // Copy constructor (used by engine for look-ahead / check detection)
    public Board(Board other) {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                Piece p = other.grid[r][c];
                if (p != null) {
                    this.grid[r][c] = clonePiece(p);
                }
            }
        this.enPassantRow = other.enPassantRow;
        this.enPassantCol = other.enPassantCol;
    } 
    
    public Piece get(int row, int col) {
        return grid[row][col];
    }

    public void set(int row, int col, Piece piece) {
        grid[row][col] = piece;
    }

    public boolean inBounds(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    public boolean isEmpty(int row, int col) {
        return inBounds(row, col) && grid[row][col] == null;
    }

    public int getEnPassantRow() { return enPassantRow; }
    public int getEnPassantCol() { return enPassantCol; }

    public void setEnPassant(int row, int col) {
        enPassantRow = row;
        enPassantCol = col;
    }

    public void clearEnPassant() {
        enPassantRow = -1;
        enPassantCol = -1;
    }

    public void applyMove(Move move) {
        Piece moving = grid[move.fromRow][move.fromCol];
        clearEnPassant();

        switch (move.flag) {
            case CASTLE_KINGSIDE -> {
                int row = move.fromRow;
                grid[row][6] = grid[row][4];   // king
                grid[row][5] = grid[row][7];   // rook
                grid[row][4] = null;
                grid[row][7] = null;
                grid[row][6].setMoved();
                grid[row][5].setMoved();
            }
            case CASTLE_QUEENSIDE -> {
                int row = move.fromRow;
                grid[row][2] = grid[row][4];   // king
                grid[row][3] = grid[row][0];   // rook
                grid[row][4] = null;
                grid[row][0] = null;
                grid[row][2].setMoved();
                grid[row][3].setMoved();
            }
            case EN_PASSANT -> {
                grid[move.toRow][move.toCol]     = moving;
                grid[move.fromRow][move.fromCol] = null;
                grid[move.fromRow][move.toCol]   = null; // captured pawn
                moving.setMoved();
            }
            case PROMOTION -> {
                grid[move.toRow][move.toCol]     = createPromoted(move.promotionPiece, moving.getColor());
                grid[move.fromRow][move.fromCol] = null;
            }
            default -> {
                // Track en-passant opportunity for a double pawn push
                if (moving.getType() == Piece.Type.PAWN
                        && Math.abs(move.toRow - move.fromRow) == 2) {
                    setEnPassant((move.fromRow + move.toRow) / 2, move.fromCol);
                }
                grid[move.toRow][move.toCol]     = moving;
                grid[move.fromRow][move.fromCol] = null;
                moving.setMoved();
            }
        }
    }

    // Find king position 

    public int[] findKing(Piece.Color color) {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p != null && p.getType() == Piece.Type.KING && p.getColor() == color)
                    return new int[]{r, c};
            }
        throw new IllegalStateException("King not found for " + color);
    }

    // Board setup 

    private void init() {
        // Black back rank
        placeMajorPieces(0, Piece.Color.BLACK);
        placePawns(1, Piece.Color.BLACK);

        // White back rank
        placePawns(6, Piece.Color.WHITE);
        placeMajorPieces(7, Piece.Color.WHITE);
    }

    private void placeMajorPieces(int row, Piece.Color color) {
        grid[row][0] = new Rook(color);
        grid[row][1] = new Knight(color);
        grid[row][2] = new Bishop(color);
        grid[row][3] = new Queen(color);
        grid[row][4] = new King(color);
        grid[row][5] = new Bishop(color);
        grid[row][6] = new Knight(color);
        grid[row][7] = new Rook(color);
    }

    private void placePawns(int row, Piece.Color color) {
        for (int c = 0; c < 8; c++) grid[row][c] = new Pawn(color);
    }

    // Helpers 
    private Piece clonePiece(Piece p) {
        Piece clone = createPromoted(p.getType(), p.getColor());
        if (p.hasMoved()) clone.setMoved();
        return clone;
    }

    private Piece createPromoted(Piece.Type type, Piece.Color color) {
        return switch (type) {
            case KING   -> new King(color);
            case QUEEN  -> new Queen(color);
            case ROOK   -> new Rook(color);
            case BISHOP -> new Bishop(color);
            case KNIGHT -> new Knight(color);
            case PAWN   -> new Pawn(color);
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("  a  b  c  d  e  f  g  h\n");
        for (int r = 0; r < 8; r++) {
            sb.append(8 - r).append(' ');
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                sb.append(p == null ? " . " : p + " ");
            }
            sb.append(8 - r).append('\n');
        }
        sb.append("  a  b  c  d  e  f  g  h\n");
        return sb.toString();
    }
}
