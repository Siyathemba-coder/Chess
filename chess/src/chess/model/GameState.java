package chess.model;

import java.util.ArrayDeque;
import java.util.Deque;

public class GameState {

    public enum Status { ONGOING, CHECK, CHECKMATE, STALEMATE, DRAW_50_MOVE }

    private Piece.Color currentTurn = Piece.Color.WHITE;
    private Status status = Status.ONGOING;
    private final Deque<Move> history = new ArrayDeque<>();
    private int halfMoveClock = 0; // for 50-move rule

    public Piece.Color getCurrentTurn() { return currentTurn; }
    public Status      getStatus()      { return status; }
    public Deque<Move> getHistory()     { return history; }

    public void recordMove(Move move, boolean wasCapture, boolean wasPawnMove) {
        history.push(move);
        if (wasCapture || wasPawnMove) halfMoveClock = 0;
        else halfMoveClock++;
    }

    public void switchTurn() {
        currentTurn = (currentTurn == Piece.Color.WHITE)
                ? Piece.Color.BLACK : Piece.Color.WHITE;
    }

    public void setStatus(Status status) { this.status = status; }

    public boolean isOver() {
        return status == Status.CHECKMATE
                || status == Status.STALEMATE
                || status == Status.DRAW_50_MOVE;
    }

    public boolean isFiftyMoveRule() { return halfMoveClock >= 100; }
}
