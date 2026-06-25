package chess.gui;

import chess.engine.GameEngine;
import chess.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Custom-painted chess board panel.
 * Handles click-to-select and click-to-move interactions.
 */
public class BoardPanel extends JPanel {

    // Board colours 
    private static final Color LIGHT         = new Color(0xF0D9B5);
    private static final Color DARK          = new Color(0xB58863);
    private static final Color SELECT_TINT   = new Color(246, 246, 105, 160);
    private static final Color MOVE_DOT      = new Color( 26,  26,  26,  70);
    private static final Color CAPTURE_RING  = new Color( 26,  26,  26,  70);
    private static final Color CHECK_TINT    = new Color(233,  69,  96, 140);
    private static final Color COORD_DARK    = new Color(240, 217, 181, 200);
    private static final Color COORD_LIGHT   = new Color(181, 136,  99, 200);
    private static final Color BOARD_BORDER  = new Color(0x6B3F1E);
    private static final Color LAST_MOVE     = new Color(205, 209, 110, 160);

    private static final int SQUARE = 76;
    private static final int COORD  = 18;  // coordinate label gutter
    private static final int PAD    = 4;   // outer board padding

    private final GameEngine  engine;
    private final ChessFrame  frame;

    private int selectedRow = -1;
    private int selectedCol = -1;
    private final Set<String> legalTargets = new HashSet<>();

    // Last move squares for highlight
    private int lastFromRow = -1, lastFromCol = -1;
    private int lastToRow   = -1, lastToCol   = -1;

    private boolean whiteAtBottom = true; // perspective

    public BoardPanel(GameEngine engine, ChessFrame frame) {
        this.engine = engine;
        this.frame  = frame;

        int size = SQUARE * 8 + COORD * 2 + PAD * 2;
        setPreferredSize(new Dimension(size, size));
        setBackground(ChessFrame.BG);

        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { handleClick(e.getX(), e.getY()); }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,       RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,  RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Outer border / frame
        int boardPx = SQUARE * 8 + COORD * 2;
        g2.setColor(BOARD_BORDER);
        g2.fillRoundRect(PAD - 2, PAD - 2, boardPx + 4, boardPx + 4, 8, 8);

        Board board = engine.getBoard();
        GameState.Status status = engine.getState().getStatus();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int dr = whiteAtBottom ? row : 7 - row;
                int dc = whiteAtBottom ? col : 7 - col;

                int x = PAD + COORD + dc * SQUARE;
                int y = PAD + COORD + dr * SQUARE;

                // Base square colour
                boolean isLight = (row + col) % 2 == 0;
                g2.setColor(isLight ? LIGHT : DARK);
                g2.fillRect(x, y, SQUARE, SQUARE);

                // Last-move highlight
                if ((row == lastFromRow && col == lastFromCol)
                        || (row == lastToRow && col == lastToCol)) {
                    g2.setColor(LAST_MOVE);
                    g2.fillRect(x, y, SQUARE, SQUARE);
                }

                // Selection highlight
                if (row == selectedRow && col == selectedCol) {
                    g2.setColor(SELECT_TINT);
                    g2.fillRect(x, y, SQUARE, SQUARE);
                }

                // Check tint on king in check
                if (status == GameState.Status.CHECK || status == GameState.Status.CHECKMATE) {
                    Piece p = board.get(row, col);
                    if (p != null && p.getType() == Piece.Type.KING
                            && p.getColor() == engine.getState().getCurrentTurn()) {
                        g2.setColor(CHECK_TINT);
                        g2.fillRect(x, y, SQUARE, SQUARE);
                    }
                }

                // Legal-move indicators
                String key = row + "," + col;
                if (legalTargets.contains(key)) {
                    Piece target = board.get(row, col);
                    if (target == null) {
                        // Dot in centre
                        int r = SQUARE / 6;
                        g2.setColor(MOVE_DOT);
                        g2.fillOval(x + SQUARE / 2 - r, y + SQUARE / 2 - r, r * 2, r * 2);
                    } else {
                        // Ring around existing piece (capture)
                        int t = 4;
                        g2.setColor(CAPTURE_RING);
                        g2.setStroke(new BasicStroke(t));
                        g2.drawOval(x + t / 2, y + t / 2, SQUARE - t, SQUARE - t);
                        g2.setStroke(new BasicStroke(1));
                    }
                }

                // Piece
                Piece piece = board.get(row, col);
                if (piece != null) drawPiece(g2, piece, x, y);

                // Coordinate labels
                Font coordFont = new Font("Georgia", Font.BOLD, 11);
                g2.setFont(coordFont);

                // Rank numbers (left column only)
                if (dc == 0) {
                    int rank = whiteAtBottom ? 8 - row : row + 1;
                    g2.setColor(isLight ? COORD_LIGHT : COORD_DARK);
                    g2.drawString(String.valueOf(rank), x + 3, y + 14);
                }
                // File letters (bottom row only)
                if (dr == 7) {
                    char file = (char) ('a' + (whiteAtBottom ? col : 7 - col));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.setColor(isLight ? COORD_LIGHT : COORD_DARK);
                    g2.drawString(String.valueOf(file), x + SQUARE - fm.charWidth(file) - 3, y + SQUARE - 3);
                }
            }
        }
    }

    // Piece rendering (Unicode glyphs, styled) 

    private void drawPiece(Graphics2D g2, Piece piece, int x, int y) {
        String glyph = getGlyph(piece);
        boolean isWhite = piece.getColor() == Piece.Color.WHITE;

        // Shadow
        g2.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 52));
        FontMetrics fm = g2.getFontMetrics();
        int tx = x + (SQUARE - fm.stringWidth(glyph)) / 2;
        int ty = y + (SQUARE + fm.getAscent() - fm.getDescent()) / 2 - 2;

        g2.setColor(new Color(0, 0, 0, 60));
        g2.drawString(glyph, tx + 2, ty + 2);

        // Outline for white pieces
        if (isWhite) {
            g2.setColor(new Color(0x3A2A1A));
            for (int dx = -1; dx <= 1; dx++)
                for (int dy = -1; dy <= 1; dy++)
                    if (dx != 0 || dy != 0)
                        g2.drawString(glyph, tx + dx, ty + dy);
        }

        // Piece fill
        g2.setColor(isWhite ? new Color(0xFFFAF0) : new Color(0x1A1A2E));
        g2.drawString(glyph, tx, ty);
    }

    private String getGlyph(Piece piece) {
        boolean white = piece.getColor() == Piece.Color.WHITE;
        return switch (piece.getType()) {
            case KING   -> white ? "♔" : "♚";
            case QUEEN  -> white ? "♕" : "♛";
            case ROOK   -> white ? "♖" : "♜";
            case BISHOP -> white ? "♗" : "♝";
            case KNIGHT -> white ? "♘" : "♞";
            case PAWN   -> white ? "♙" : "♟";
        };
    }

    // Interaction 

    private void handleClick(int px, int py) {
        if (engine.getState().isOver()) return;

        int col = (px - PAD - COORD) / SQUARE;
        int row = (py - PAD - COORD) / SQUARE;
        if (!whiteAtBottom) { row = 7 - row; col = 7 - col; }
        if (row < 0 || row > 7 || col < 0 || col > 7) return;

        Board board = engine.getBoard();
        Piece clicked = board.get(row, col);

        if (selectedRow == -1) {
            // Nothing selected yet - select own piece
            if (clicked != null && clicked.getColor() == engine.getState().getCurrentTurn()) {
                selectPiece(row, col);
            }
        } else {
            String key = row + "," + col;
            if (legalTargets.contains(key)) {
                // Execute move
                executeMove(selectedRow, selectedCol, row, col);
            } else if (clicked != null && clicked.getColor() == engine.getState().getCurrentTurn()) {
                // Switch selection to another own piece
                selectPiece(row, col);
            } else {
                // Deselect
                clearSelection();
            }
        }
        repaint();
    }

    private void selectPiece(int row, int col) {
        selectedRow = row;
        selectedCol = col;
        legalTargets.clear();
        List<Move> moves = engine.getLegalMovesFor(row, col);
        for (Move m : moves) legalTargets.add(m.toRow + "," + m.toCol);
    }

    private void clearSelection() {
        selectedRow = -1;
        selectedCol = -1;
        legalTargets.clear();
    }

    void executeMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece moving = engine.getBoard().get(fromRow, fromCol);
        boolean wasWhite = moving.getColor() == Piece.Color.WHITE;

        // Promotion dialog
        Piece.Type promotionChoice = Piece.Type.QUEEN;
        if (engine.isPromotion(fromRow, fromCol, toRow, toCol)) {
            PromotionDialog dlg = new PromotionDialog(
                    SwingUtilities.getWindowAncestor(this), moving.getColor());
            promotionChoice = dlg.getChoice();
        }

        String notation = buildNotation(fromRow, fromCol, toRow, toCol, promotionChoice);
        boolean moved = engine.makeMove(fromRow, fromCol, toRow, toCol, promotionChoice);

        if (moved) {
            lastFromRow = fromRow; lastFromCol = fromCol;
            lastToRow   = toRow;   lastToCol   = toCol;
            clearSelection();
            frame.onMoveMade(notation, wasWhite);
        }
    }

    /** Simple algebraic notation (not full SAN - good enough for the log). */
    private String buildNotation(int fr, int fc, int tr, int tc, Piece.Type promotion) {
        Piece p = engine.getBoard().get(fr, fc);
        String piece = switch (p.getType()) {
            case KING   -> "K";
            case QUEEN  -> "Q";
            case ROOK   -> "R";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case PAWN   -> "";
        };
        boolean capture = engine.getBoard().get(tr, tc) != null;
        char fromFile = (char) ('a' + fc);
        char toFile   = (char) ('a' + tc);
        String cap = capture ? "x" : "";
        if (p.getType() == Piece.Type.PAWN && capture) cap = fromFile + "x";
        String promo = "";
        if (p.getType() == Piece.Type.PAWN && (tr == 0 || tr == 7) && promotion != null) {
            promo = "=" + switch (promotion) {
                case QUEEN -> "Q"; case ROOK -> "R";
                case BISHOP -> "B"; case KNIGHT -> "N";
                default -> "Q";
            };
        }
        return piece + cap + toFile + (8 - tr) + promo;
    }
}
