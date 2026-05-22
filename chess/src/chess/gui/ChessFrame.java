package chess.gui;

import chess.engine.ChessAI;
import chess.engine.GameEngine;
import chess.model.GameState;
import chess.model.Piece;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ChessFrame extends JFrame {

    static final Color BG           = new Color(0x1A1A2E);
    static final Color PANEL_BG     = new Color(0x16213E);
    static final Color ACCENT       = new Color(0xE94560);
    static final Color TEXT_PRIMARY = new Color(0xE0E0E0);
    static final Color TEXT_DIM     = new Color(0x8892A4);
    static final Color BORDER_COLOR = new Color(0x0F3460);

    static final Font FONT_TITLE  = new Font("Georgia", Font.BOLD, 22);
    static final Font FONT_STATUS = new Font("Georgia", Font.ITALIC, 13);
    static final Font FONT_MONO   = new Font("Courier New", Font.PLAIN, 12);

    private GameEngine engine;
    private BoardPanel boardPanel;
    private JLabel     statusLabel;
    private JTextArea  moveLog;
    private JLabel     turnIndicator;
    private int        moveNumber = 1;

    // AI
    private boolean     aiEnabled = false;
    private Piece.Color aiColor   = Piece.Color.BLACK;
    private ChessAI     ai        = new ChessAI(ChessAI.Difficulty.MEDIUM);

    public ChessFrame() {
        super("Chess");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBackground(BG);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);
        buildUI();
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void buildUI() {
        getContentPane().removeAll();
        engine     = new GameEngine();
        moveNumber = 1;

        add(buildTitleBar(), BorderLayout.NORTH);

        boardPanel = new BoardPanel(engine, this);
        JPanel bw = new JPanel(new GridBagLayout());
        bw.setBackground(BG);
        bw.setBorder(new EmptyBorder(12, 20, 12, 12));
        bw.add(boardPanel);
        add(bw, BorderLayout.CENTER);

        JPanel sidebar = buildSidebar();
        add(sidebar, BorderLayout.EAST);

        statusLabel = new JLabel("White to move", SwingConstants.CENTER);
        statusLabel.setFont(FONT_STATUS);
        statusLabel.setForeground(TEXT_DIM);
        statusLabel.setBorder(new EmptyBorder(6, 0, 8, 0));
        statusLabel.setBackground(PANEL_BG);
        statusLabel.setOpaque(true);
        add(statusLabel, BorderLayout.SOUTH);

        revalidate();
        repaint();

        if (aiEnabled && aiColor == Piece.Color.WHITE)
            SwingUtilities.invokeLater(this::triggerAiMove);
    }

    // ── Title bar ──────────────────────────────────────────────────────────

    private JPanel buildTitleBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(PANEL_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(10, 20, 10, 20)));

        JLabel title = new JLabel("#  CHESS");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_PRIMARY);
        p.add(title, BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);

        JButton aiBtn = styledButton(aiEnabled ? "AI: ON  (settings)" : "AI: OFF (settings)");
        aiBtn.addActionListener(e -> showAiDialog());
        btns.add(aiBtn);

        JButton ng = styledButton("New Game");
        ng.addActionListener(e -> resetGame());
        btns.add(ng);
        p.add(btns, BorderLayout.EAST);
        return p;
    }

    // ── Sidebar ────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel s = new JPanel();
        s.setLayout(new BoxLayout(s, BoxLayout.Y_AXIS));
        s.setBackground(PANEL_BG);
        s.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, BORDER_COLOR),
                new EmptyBorder(16, 16, 16, 16)));
        s.setPreferredSize(new Dimension(210, 0));

        // Turn panel
        JPanel tp = new JPanel();
        tp.setLayout(new BoxLayout(tp, BoxLayout.Y_AXIS));
        tp.setBackground(new Color(0x0F3460));
        tp.setBorder(new EmptyBorder(10, 12, 10, 12));
        tp.setAlignmentX(LEFT_ALIGNMENT);
        tp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel tl = new JLabel("TURN");
        tl.setFont(new Font("Courier New", Font.BOLD, 10));
        tl.setForeground(TEXT_DIM);
        tl.setAlignmentX(LEFT_ALIGNMENT);

        JLabel ti = new JLabel("O White");
        ti.setFont(new Font("Georgia", Font.BOLD, 16));
        ti.setForeground(new Color(0xF5F5DC));
        ti.setAlignmentX(LEFT_ALIGNMENT);

        tp.add(tl);
        tp.add(Box.createVerticalStrut(4));
        tp.add(ti);
        s.add(tp);

        // Store turn indicator reference here, while ti is in scope
        turnIndicator = ti;

        // AI info
        if (aiEnabled) {
            s.add(Box.createVerticalStrut(8));
            JPanel ai = new JPanel();
            ai.setLayout(new BoxLayout(ai, BoxLayout.Y_AXIS));
            ai.setBackground(new Color(0x0A2540));
            ai.setBorder(new EmptyBorder(8, 12, 8, 12));
            ai.setAlignmentX(LEFT_ALIGNMENT);
            ai.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

            JLabel at = new JLabel("AI OPPONENT");
            at.setFont(new Font("Courier New", Font.BOLD, 10));
            at.setForeground(new Color(0x7EC8E3));
            at.setAlignmentX(LEFT_ALIGNMENT);

            JLabel ad = new JLabel(
                    (aiColor == Piece.Color.WHITE ? "White" : "Black")
                    + " · " + this.ai.getDifficulty().name());
            ad.setFont(new Font("Georgia", Font.PLAIN, 12));
            ad.setForeground(TEXT_DIM);
            ad.setAlignmentX(LEFT_ALIGNMENT);

            ai.add(at);
            ai.add(Box.createVerticalStrut(3));
            ai.add(ad);
            s.add(ai);
        }

        s.add(Box.createVerticalStrut(16));

        JLabel ml = new JLabel("MOVE LOG");
        ml.setFont(new Font("Courier New", Font.BOLD, 10));
        ml.setForeground(TEXT_DIM);
        ml.setAlignmentX(LEFT_ALIGNMENT);
        s.add(ml);
        s.add(Box.createVerticalStrut(6));

        moveLog = new JTextArea();
        moveLog.setEditable(false);
        moveLog.setBackground(new Color(0x0D1B2A));
        moveLog.setForeground(new Color(0x7EC8E3));
        moveLog.setFont(FONT_MONO);
        moveLog.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane sc = new JScrollPane(moveLog);
        sc.setAlignmentX(LEFT_ALIGNMENT);
        sc.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        sc.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        sc.getViewport().setBackground(new Color(0x0D1B2A));
        s.add(sc);

        s.add(Box.createVerticalStrut(16));

        JButton resign = styledButton("Resign");
        resign.setAlignmentX(LEFT_ALIGNMENT);
        resign.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        resign.addActionListener(e -> {
            if (!engine.getState().isOver()) {
                String w = engine.getState().getCurrentTurn() == Piece.Color.WHITE ? "Black" : "White";
                setStatus(w + " wins by resignation!", ACCENT);
            }
        });
        s.add(resign);
        return s;
    }

    // ── AI dialog ──────────────────────────────────────────────────────────

    private void showAiDialog() {
        JDialog dlg = new JDialog(this, "AI Settings", true);
        dlg.setUndecorated(true);

        JPanel root = new JPanel(new GridLayout(0, 1, 0, 10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x16213E));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel t = new JLabel("AI Settings", SwingConstants.CENTER);
        t.setFont(new Font("Georgia", Font.BOLD, 15));
        t.setForeground(TEXT_PRIMARY);
        root.add(t);

        JCheckBox check = new JCheckBox("Enable AI opponent", aiEnabled);
        check.setForeground(TEXT_PRIMARY);
        check.setBackground(new Color(0x16213E));
        check.setFont(new Font("Georgia", Font.PLAIN, 13));
        root.add(check);

        JPanel cr = row("AI plays:");
        JComboBox<String> colorBox = new JComboBox<>(new String[]{"White", "Black"});
        colorBox.setSelectedItem(aiColor == Piece.Color.WHITE ? "White" : "Black");
        styleCombo(colorBox); cr.add(colorBox); root.add(cr);

        JPanel dr = row("Difficulty:");
        JComboBox<String> diffBox = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        diffBox.setSelectedItem(switch (ai.getDifficulty()) {
            case EASY -> "Easy"; case HARD -> "Hard"; default -> "Medium";
        });
        styleCombo(diffBox); dr.add(diffBox); root.add(dr);

        JButton ok = styledButton("Apply & New Game");
        ok.setPreferredSize(new Dimension(160, 34));
        ok.addActionListener(e -> {
            aiEnabled = check.isSelected();
            aiColor   = colorBox.getSelectedItem().equals("White")
                    ? Piece.Color.WHITE : Piece.Color.BLACK;
            ai = new ChessAI(switch ((String) diffBox.getSelectedItem()) {
                case "Easy" -> ChessAI.Difficulty.EASY;
                case "Hard" -> ChessAI.Difficulty.HARD;
                default     -> ChessAI.Difficulty.MEDIUM;
            });
            dlg.dispose();
            resetGame();
        });
        root.add(ok);

        dlg.setContentPane(root);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private JPanel row(String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Georgia", Font.PLAIN, 13));
        l.setForeground(TEXT_DIM);
        p.add(l);
        return p;
    }

    private void styleCombo(JComboBox<?> b) {
        b.setBackground(new Color(0x0F3460));
        b.setForeground(TEXT_PRIMARY);
        b.setFont(new Font("Georgia", Font.PLAIN, 12));
    }

    // ── AI trigger ─────────────────────────────────────────────────────────

    void triggerAiMove() {
        if (!aiEnabled || engine.getState().isOver()) return;
        if (engine.getState().getCurrentTurn() != aiColor) return;

        boardPanel.setEnabled(false);
        setStatus("AI is thinking...", TEXT_DIM);

        SwingWorker<chess.model.Move, Void> worker = new SwingWorker<>() {
            @Override protected chess.model.Move doInBackground() {
                return ai.getBestMove(engine.getBoard(), aiColor);
            }
            @Override protected void done() {
                boardPanel.setEnabled(true);
                try {
                    chess.model.Move m = get();
                    if (m != null)
                        boardPanel.executeMove(m.fromRow, m.fromCol, m.toRow, m.toCol);
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        };
        worker.execute();
    }

    // ── Hooks from BoardPanel ──────────────────────────────────────────────

    void onMoveMade(String moveNotation, boolean whiteJustMoved) {
        if (whiteJustMoved) {
            moveLog.append(String.format("%3d. %-7s", moveNumber, moveNotation));
        } else {
            moveLog.append(moveNotation + "\n");
            moveNumber++;
        }
        moveLog.setCaretPosition(moveLog.getDocument().getLength());

        GameState.Status status = engine.getState().getStatus();
        Piece.Color turn = engine.getState().getCurrentTurn();

        switch (status) {
            case CHECKMATE -> {
                String w = (turn == Piece.Color.WHITE) ? "Black" : "White";
                setStatus("* Checkmate — " + w + " wins!", ACCENT);
                updateTurnIndicator("* " + w + " wins", ACCENT);
            }
            case STALEMATE -> { setStatus("Stalemate - Draw", TEXT_DIM); updateTurnIndicator("Draw", TEXT_DIM); }
            case DRAW_50_MOVE -> { setStatus("50-move rule - Draw", TEXT_DIM); updateTurnIndicator("Draw", TEXT_DIM); }
            case CHECK -> {
                String who = (turn == Piece.Color.WHITE) ? "White" : "Black";
                setStatus("! " + who + " is in check!", new Color(0xFFB347));
                updateTurnIndicator((turn == Piece.Color.WHITE ? "O White" : "O Black") + " !", new Color(0xFFB347));
            }
            default -> {
                String who = (turn == Piece.Color.WHITE) ? "White" : "Black";
                setStatus(who + " to move", TEXT_DIM);
                boolean iw = (turn == Piece.Color.WHITE);
                updateTurnIndicator(iw ? "O White" : "O Black",
                        iw ? new Color(0xF5F5DC) : new Color(0xA0A0A0));
            }
        }

        if (!engine.getState().isOver() && aiEnabled
                && engine.getState().getCurrentTurn() == aiColor)
            triggerAiMove();
    }

    void updateTurnIndicator(String text, Color color) {
        if (turnIndicator != null) { turnIndicator.setText(text); turnIndicator.setForeground(color); }
    }

    void setStatus(String text, Color color) {
        if (statusLabel != null) { statusLabel.setText(text); statusLabel.setForeground(color); }
    }

    private void resetGame() { buildUI(); pack(); }

    // ── Entry point ────────────────────────────────────────────────────────

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(ChessFrame::new);
    }

    // ── Shared button factory ──────────────────────────────────────────────

    JButton styledButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed()  ? ACCENT.darker()
                         : getModel().isRollover() ? ACCENT
                         : new Color(0x0F3460);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(TEXT_PRIMARY);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        b.setFont(new Font("Georgia", Font.BOLD, 12));
        b.setForeground(TEXT_PRIMARY);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(130, 32));
        return b;
    }
}
