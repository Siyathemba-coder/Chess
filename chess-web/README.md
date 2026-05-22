# Chess — Full Stack Java

## What's included

| Feature | Status |
|---|---|
| Promotion dialog | ✅ Custom styled dialog (Queen/Rook/Bishop/Knight) |
| AI opponent | ✅ Minimax + alpha-beta pruning + iterative deepening + move ordering |
| Web app | ✅ Spring Boot REST + WebSocket backend + React frontend |

---

## Project structure

```
chess/                         ← Desktop (Swing) app
  src/chess/
    model/                     ← Board, Piece, Move, GameState
    model/pieces/              ← King Queen Rook Bishop Knight Pawn
    engine/
      GameEngine.java          ← makeMove(from, to, promotionPiece)
      MoveValidator.java       ← legal move generation + check detection
      Evaluator.java           ← material values + piece-square tables
      ChessAI.java             ← minimax, alpha-beta, iterative deepening
    gui/
      ChessFrame.java          ← main window + AI settings dialog
      BoardPanel.java          ← custom-painted board + click handling
      PromotionDialog.java     ← styled promotion picker

chess-web/                     ← Web app
  pom.xml                      ← Spring Boot 3.2
  src/main/java/com/chess/
    ChessApplication.java
    WebSocketConfig.java
    controller/GameController.java
    service/GameService.java
    dto/                       ← MoveRequest, GameStateDto, etc.
  frontend/                    ← React + Vite
    src/App.jsx                ← setup screen + game UI + promo dialog
    src/components/
      ChessBoard.jsx           ← board renderer
      useChessGame.js          ← REST + WebSocket hook
```

---

## Run the Desktop (Swing) app

```bash
cd chess
mkdir out
# Compile everything
javac -d out -sourcepath src $(find src -name "*.java")
# Launch
java -cp out chess.gui.ChessFrame
```

**AI settings:** click the **AI: OFF ⚙** button in the title bar.  
Choose color (White/Black) and difficulty (Easy / Medium / Hard).  
Hard = depth-4 minimax with quiescence search.

---

## Run the Web app

### 1. Copy model + engine into the Spring Boot source tree

```bash
# The Spring Boot project needs the chess model/engine classes
cp -r chess/src/chess chess-web/src/main/java/
```

### 2. Start Spring Boot backend

```bash
cd chess-web
mvn spring-boot:run
# Listens on http://localhost:8080
```

### 3. Start React frontend

```bash
cd chess-web/frontend
npm install
npm run dev
# Opens http://localhost:5173
```

The Vite dev server proxies `/api` and `/ws` to `localhost:8080`.

---

## REST API reference

| Method | URL | Body | Description |
|---|---|---|---|
| POST | `/api/game/new` | `{aiEnabled, aiColor, aiDifficulty}` | Create game |
| GET  | `/api/game/{id}` | — | Current state |
| POST | `/api/game/move` | `{gameId, fromRow, fromCol, toRow, toCol, promotionPiece}` | Make move |
| GET  | `/api/game/{id}/legal-moves?row=&col=` | — | Legal targets for piece |

### WebSocket

Connect via SockJS to `/ws`, subscribe to `/topic/game/{gameId}`.  
Send moves to `/app/move` with the same `MoveRequest` JSON.  
Every move (including AI moves) broadcasts the full `GameStateDto` to all subscribers.

---

## AI design

```
ChessAI.getBestMove(board, color)
  └─ Iterative deepening (depth 1..maxDepth)
      └─ negamax(board, depth, α, β, color)
          ├─ Move ordering: captures first (MVV-LVA), then quiet moves
          ├─ Alpha-beta pruning (cuts ~50-60% of tree)
          └─ depth==0 → quiescence search (captures only, avoids horizon effect)

Evaluation:
  score = Σ (material[piece] + PST[piece][square]) for own pieces
        - Σ (material[piece] + PST[piece][square]) for opponent pieces

Depth/strength:
  EASY   = depth 2  (~hundreds of nodes)
  MEDIUM = depth 3  (~thousands of nodes)
  HARD   = depth 4  (~tens of thousands of nodes)
```

---

## Next steps

- [ ] **Opening book** — hard-code 10-15 common openings for depth-0
- [ ] **Transposition table** — cache board hashes to skip re-evaluation
- [ ] **User accounts** — Spring Security + PostgreSQL for saved games
- [ ] **Game history** — persist games to DB, replay mode
- [ ] **Online multiplayer** — match players via WebSocket room pairing
- [ ] **Mobile** — the React frontend is already responsive
