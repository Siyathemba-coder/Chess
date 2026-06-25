package chess.engine;

import chess.model.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Chess AI using:
 *  - Minimax with alpha-beta pruning
 *  - Iterative deepening (searches depth 1..maxDepth, returns best found)
 *  - Move ordering (captures first, sorted by MVV-LVA; then quiet moves)
 *  - Piece-square table + material evaluation
 *
 * Thread-safe for a single game (one instance per game).
 */
public class ChessAI {

    public enum Difficulty {
        EASY(2), MEDIUM(3), HARD(4);
        public final int depth;
        Difficulty(int d) { this.depth = d; }
    }

    private final MoveValidator validator = new MoveValidator();
    private       Difficulty    difficulty;

    // Search stats (reset each move)
    private int nodesVisited;

    public ChessAI(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void setDifficulty(Difficulty d) { this.difficulty = d; }
    public Difficulty getDifficulty()       { return difficulty; }

    // -- Public API --
    /**
     * Computes the best move for the given color on the given board.
     * Returns null if no legal moves exist (shouldn't happen in normal play).
     */
    public Move getBestMove(Board board, Piece.Color color) {
        nodesVisited = 0;
        Move best = null;
        int bestScore = Integer.MIN_VALUE;

        List<Move> moves = orderMoves(validator.getAllLegalMoves(color, board), board);
        if (moves.isEmpty()) return null;

        // Iterative deepening - always have a result even if time runs out
        for (int depth = 1; depth <= difficulty.depth; depth++) {
            int alpha = Integer.MIN_VALUE + 1;
            int beta  = Integer.MAX_VALUE - 1;
            Move depthBest = null;
            int depthBestScore = Integer.MIN_VALUE;

            for (Move m : moves) {
                Board copy = new Board(board);
                copy.applyMove(m);
                int score = -negamax(copy, depth - 1, -beta, -alpha,
                        opponent(color));
                if (score > depthBestScore) {
                    depthBestScore = score;
                    depthBest      = m;
                }
                if (score > alpha) alpha = score;
            }

            if (depthBest != null) {
                best      = depthBest;
                bestScore = depthBestScore;
            }

            // Early exit on forced mate
            if (bestScore >= Evaluator.CHECKMATE - 100) break;
        }

        return best;
    }

    public int getLastNodesVisited() { return nodesVisited; }

    // Negamax with alpha-beta 

    private int negamax(Board board, int depth, int alpha, int beta, Piece.Color color) {
        nodesVisited++;

        if (depth == 0) return quiescence(board, alpha, beta, color);

        List<Move> moves = orderMoves(validator.getAllLegalMoves(color, board), board);

        if (moves.isEmpty()) {
            return validator.isInCheck(color, board)
                    ? -(Evaluator.CHECKMATE - (difficulty.depth - depth)) // checkmate
                    : 0;                                                   // stalemate
        }

        for (Move m : moves) {
            Board copy = new Board(board);
            copy.applyMove(m);
            int score = -negamax(copy, depth - 1, -beta, -alpha, opponent(color));
            if (score >= beta) return beta;  // beta cutoff
            if (score > alpha) alpha = score;
        }

        return alpha;
    }

    /**
     * Quiescence search: only explore captures to avoid horizon effect.
     */
    private int quiescence(Board board, int alpha, int beta, Piece.Color color) {
        nodesVisited++;
        int standPat = evaluate(board, color);
        if (standPat >= beta) return beta;
        if (standPat > alpha) alpha = standPat;

        List<Move> captures = capturesOnly(validator.getAllLegalMoves(color, board), board);
        for (Move m : captures) {
            Board copy = new Board(board);
            copy.applyMove(m);
            int score = -quiescence(copy, -beta, -alpha, opponent(color));
            if (score >= beta) return beta;
            if (score > alpha) alpha = score;
        }
        return alpha;
    }
    /**
     * Evaluates the board from color's perspective (positive = good for color).
     */
    private int evaluate(Board board, Piece.Color color) {
        int score = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.get(r, c);
                if (p == null) continue;
                int val = Evaluator.material(p.getType())
                        + Evaluator.pst(p.getType(), p.getColor(), r, c);
                score += (p.getColor() == color) ? val : -val;
            }
        }
        return score;
    }

    // Move ordering (MVV-LVA for captures) 

    private List<Move> orderMoves(List<Move> moves, Board board) {
        List<Move> captures = new ArrayList<>();
        List<Move> quiet    = new ArrayList<>();

        for (Move m : moves) {
            if (board.get(m.toRow, m.toCol) != null || m.flag == Move.Flag.EN_PASSANT) {
                captures.add(m);
            } else {
                quiet.add(m);
            }
        }

        // MVV-LVA: sort captures by (victim value - attacker value) descending
        captures.sort(Comparator.comparingInt((Move m) -> mvvLva(m, board)).reversed());

        List<Move> ordered = new ArrayList<>(captures.size() + quiet.size());
        ordered.addAll(captures);
        ordered.addAll(quiet);
        return ordered;
    }

    private List<Move> capturesOnly(List<Move> moves, Board board) {
        List<Move> result = new ArrayList<>();
        for (Move m : moves)
            if (board.get(m.toRow, m.toCol) != null || m.flag == Move.Flag.EN_PASSANT)
                result.add(m);
        return result;
    }

    private int mvvLva(Move m, Board board) {
        Piece victim   = board.get(m.toRow, m.toCol);
        Piece attacker = board.get(m.fromRow, m.fromCol);
        int victimVal   = victim   != null ? Evaluator.material(victim.getType())   : 0;
        int attackerVal = attacker != null ? Evaluator.material(attacker.getType()) : 0;
        return victimVal * 10 - attackerVal;
    }

    private Piece.Color opponent(Piece.Color c) {
        return c == Piece.Color.WHITE ? Piece.Color.BLACK : Piece.Color.WHITE;
    }
}
