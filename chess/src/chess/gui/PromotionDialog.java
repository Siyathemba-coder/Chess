package chess.gui;

import chess.model.Piece;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Modal dialog that lets the player choose a promotion piece.
 * Styled to match ChessFrame's dark palette.
 */
public class PromotionDialog extends JDialog {

    private Piece.Type chosen = Piece.Type.QUEEN; // safe default

    // Glyphs indexed by color
    private static final String[] WHITE_GLYPHS = { "♕", "♖", "♗", "♘" };
    private static final String[] BLACK_GLYPHS = { "♛", "♜", "♝", "♞" };
    private static final Piece.Type[] TYPES = {
        Piece.Type.QUEEN, Piece.Type.ROOK, Piece.Type.BISHOP, Piece.Type.KNIGHT
    };
    private static final String[] LABELS = { "Queen", "Rook", "Bishop", "Knight" };

    public PromotionDialog(Window owner, Piece.Color color) {
        super(owner, "Promote Pawn", ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        JPanel root = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x16213E));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(0x0F3460));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Title
        JLabel title = new JLabel("Choose Promotion", SwingConstants.CENTER);
        title.setFont(new Font("Georgia", Font.BOLD, 14));
        title.setForeground(new Color(0xE0E0E0));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        root.add(title, BorderLayout.NORTH);

        // Piece buttons
        JPanel buttons = new JPanel(new GridLayout(1, 4, 10, 0));
        buttons.setOpaque(false);

        String[] glyphs = color == Piece.Color.WHITE ? WHITE_GLYPHS : BLACK_GLYPHS;

        for (int i = 0; i < 4; i++) {
            final Piece.Type type = TYPES[i];
            final String glyph   = glyphs[i];
            final String label   = LABELS[i];

            JPanel card = new JPanel(new BorderLayout(0, 4)) {
                boolean hovered = false;

                {
                    setOpaque(false);
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

                    addMouseListener(new MouseAdapter() {
                        @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                        @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                        @Override public void mousePressed(MouseEvent e) {
                            chosen = type;
                            dispose();
                        }
                    });
                }

                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = hovered ? new Color(0xE94560) : new Color(0x0F3460);
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };

            // Glyph label
            JLabel glyphLabel = new JLabel(glyph, SwingConstants.CENTER);
            glyphLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 42));
            glyphLabel.setForeground(color == Piece.Color.WHITE
                    ? new Color(0xFFFAF0) : new Color(0x1A1A2E));
            glyphLabel.setOpaque(false);

            // Name label
            JLabel nameLabel = new JLabel(label, SwingConstants.CENTER);
            nameLabel.setFont(new Font("Georgia", Font.PLAIN, 11));
            nameLabel.setForeground(new Color(0xA0A8B8));
            nameLabel.setOpaque(false);

            card.add(glyphLabel, BorderLayout.CENTER);
            card.add(nameLabel,  BorderLayout.SOUTH);
            buttons.add(card);
        }

        root.add(buttons, BorderLayout.CENTER);
        setContentPane(root);
        pack();
        setLocationRelativeTo(owner);
    }

    /** Blocks until the player chooses; returns the chosen piece type. */
    public Piece.Type getChoice() {
        setVisible(true); // blocks (modal)
        return chosen;
    }
}
