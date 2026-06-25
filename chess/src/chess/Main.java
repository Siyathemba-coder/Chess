package chess;

import chess.engine.GameEngine;
import chess.model.Move;

import java.util.List;

/**
 * Quick console smoke-test — exercises the engine without a GUI.
 * Run: javac -d out -sourcepath src src/chess/Main.java && java -cp out chess.Main
 */
public class Main {

    public static void main(String[] args) {
        GameEngine engine = new GameEngine();

        System.out.println("=== Chess Engine Smoke Test ===\n");
        System.out.println(engine.getBoard());

        // e2 -> e4
        move(engine, 6, 4, 4, 4);
        // e7 -> e5
        move(engine, 1, 4, 3, 4);
        // g1 -> f3
        move(engine, 7, 6, 5, 5);
        // b8 -> c6
        move(engine, 0, 1, 2, 2);
        // f1 -> c4  (Scholar's mate setup)
        move(engine, 7, 5, 4, 2);
        // d7 -> d6
        move(engine, 1, 3, 2, 3);
        // d1 -> h5
        move(engine, 7, 3, 3, 7);
        // a7 -> a6
        move(engine, 1, 0, 2, 0);
        // Qh5 -> f7  Scholar's mate
        move(engine, 3, 7, 1, 5);

        System.out.println("\nFinal status: " + engine.getState().getStatus());
        System.out.println(engine.getBoard());
    }

    private static void move(GameEngine engine, int fr, int fc, int tr, int tc) {
        boolean ok = engine.makeMove(fr, fc, tr, tc);
        System.out.printf("Move %s%d→%s%d : %s  |  Status: %s%n",
                col(fc), 8 - fr, col(tc), 8 - tr,
                ok ? "OK" : "ILLEGAL",
                engine.getState().getStatus());
        if (ok) System.out.println(engine.getBoard());
    }

    private static String col(int c) {
        return String.valueOf((char) ('a' + c));
    }
}
