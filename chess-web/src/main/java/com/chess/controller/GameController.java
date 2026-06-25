package com.chess.controller;

import com.chess.dto.*;
import com.chess.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    // REST endpoints

    /** POST /api/game/new -> creates game, returns initial state */
    @PostMapping("/new")
    public ResponseEntity<GameStateDto> newGame(@RequestBody NewGameRequest req) {
        return ResponseEntity.ok(gameService.newGame(req));
    }

    /** GET /api/game/{id} -> current state */
    @GetMapping("/{id}")
    public ResponseEntity<GameStateDto> getGame(@PathVariable String id) {
        return gameService.getGame(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** POST /api/game/move -> apply a move, returns updated state */
    @PostMapping("/move")
    public ResponseEntity<GameStateDto> move(@RequestBody MoveRequest req) {
        return ResponseEntity.ok(gameService.applyMove(req));
    }

    /** GET /api/game/{id}/legal-moves?row=6&col=4 -> list of [row,col] targets */
    @GetMapping("/{id}/legal-moves")
    public ResponseEntity<LegalMovesDto> legalMoves(
            @PathVariable String id,
            @RequestParam int row,
            @RequestParam int col) {
        return ResponseEntity.ok(gameService.legalMoves(id, row, col));
    }

    // WebSocket 

    /**
     * Clients can also send moves via WebSocket.
     * Message destination: /app/move
     * Broadcasts updated state to /topic/game/{gameId}
     */
    @MessageMapping("/move")
    public void wsMoveMove(MoveRequest req) {
        gameService.applyMove(req);
        // GameService.applyMove already broadcasts via SimpMessagingTemplate
    }
}
