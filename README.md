# Assignment #01 — Poool Game
**PCD a.y. 2024-2025 — ISI LM UNIBO — Cesena Campus**

---

## High-Level Architecture: MVC + Active Components

The architecture follows a layered **MVC** pattern where concurrency lives in dedicated active components, combining the physics loop approach (sketch01) and the MVC async input approach (sketch02).

```
┌─────────────────────────────────────────────┐
│                  GameView (Swing/AWT)        │  ← Passive renderer
└─────────────────────┬───────────────────────┘
│ reads (snapshot)
┌─────────────────────▼───────────────────────┐
│              GameModel (Monitor)             │  ← Shared mutable state
│  - List<Ball> balls                          │
│  - int humanScore, botScore                  │
│  - GameStatus status                         │
│  + synchronized getters/setters              │
└───┬───────────────┬───────────────┬──────────┘
│               │               │
┌───▼────┐  ┌───────▼──────┐  ┌────▼──────────┐
│Physics │  │  BotAgent    │  │ InputController│
│Engine  │  │  Thread      │  │ (Keyboard)     │
│Thread  │  │  (async)     │  │ (Swing EDT)    │
└────────┘  └──────────────┘  └───────────────┘
```


---

## Core Entities (Model Layer)

- **`Ball`**: position `(x,y)`, velocity `(vx,vy)`, radius, type (`SMALL`, `HUMAN`, `BOT`); methods `move(dt)`, `applyFriction(dt)`, `checkBorderBounce()`
- **`GameModel`** (the monitor): holds `List<Ball>`, scores, `GameStatus` enum (`PLAYING`, `HUMAN_WINS`, `BOT_WINS`), hole positions. All mutating methods are `synchronized` — this is the custom monitor the assignment requires.
- **`PhysicsEngine`**: stateless utility class — elastic collision math, hole detection, friction decay. No threading inside, called by the loop thread.

---

## Version 1 — Multithreaded Architecture

Four platform threads, all interacting through `GameModel` as a monitor:

| Thread | Responsibility | Rate |
|---|---|---|
| `GameLoopThread` | Steps physics, checks holes/win condition, triggers repaint | ~60 fps |
| `BotThread` | Decides bot impulse direction & timing, updates bot ball velocity | Asynchronous |
| Swing EDT | Handles `KeyListener` impulses, updates human ball velocity | Event-driven |
| Swing Repaint Thread | Calls `view.repaint()` with a state snapshot | On demand |

The `GameLoopThread` is the core loop — it calls `physicsEngine.step(model, dt)` which internally:
1. Moves all balls by `dt`
2. Applies friction
3. Checks ball-ball collisions
4. Checks hole detection

**Synchronization**: `GameModel` uses `wait()`/`notifyAll()` for the game-over condition (bot/human threads wait, loop thread notifies when status changes). This is the custom monitor implementation.

---

## Version 2 — Executor Framework

Replace the single `GameLoopThread` loop body with parallel sub-tasks using `ExecutorService`:
```
ScheduledExecutorService scheduler  ← ticks at fixed rate
│
▼
PhysicsStepTask (Callable)
│
├── partition balls into N batches (N = # cores)
│         └── submit N MoveTask(batch) → List<Future>
│              [parallel: move + friction per ball]
│
├── join all futures (get())
│
└── CollisionDetectionTask (single-threaded phase)
└── resolve collisions, holes, scores
```


Key design choice: **movement/friction is parallelizable** (no shared writes between balls), but **collision detection/resolution is a sequential critical section** to avoid data races. This maps cleanly onto a fork-join pattern.

---

## Concurrency Analysis

The main concurrent aspects to address in the report:

- **Shared mutable state**: `GameModel` is accessed by 3+ threads simultaneously — needs the monitor
- **Asynchronous input**: keyboard events arrive on the Swing EDT, independent of the physics tick rate
- **Bot asynchrony**: bot throws happen on its own schedule, completely independent of the human player
- **Physics parallelism**: with thousands of balls, splitting movement computation across cores gives real speedup (benchmark this as the "sequential vs. concurrent" test)
- **Data race on ball list**: if the view reads while the loop writes, you get rendering glitches — solve with a **snapshot copy** of the ball list for rendering, taken inside a `synchronized` block

---

## Suggested Package Structure
```
it.unibo.poool/
├── model/
│   ├── Ball.java
│   ├── GameModel.java        ← the monitor
│   └── GameStatus.java
├── physics/
│   └── PhysicsEngine.java
├── view/
│   └── GameView.java
├── controller/
│   ├── InputController.java
│   └── GameController.java
├── concurrent/
│   ├── v1/
│   │   ├── GameLoopThread.java
│   │   └── BotThread.java
│   └── v2/
│       ├── GameLoopTask.java
│       └── MoveTask.java
└── Main.java
```
___

## Key Rule for the Model Layer
The dependency arrow must always point inward:
```
view → controller → model ← physics
                        ↑
                    concurrent
```
This guarantees that the model can always be tested headlessly, which also makes JPF verification significantly easier since you can run the model checker on pure model code without Swing on the classpath.

---
## Life Cycle of Each Frame
```
GameLoopThread                    Swing EDT (view)
│                                │
│  synchronized {                │
│    physicsEngine.step(...)     │
│    checkHoles(...)             │
│    updateScores(...)           │
│  }                             │
│                                │
│  synchronized {                │
│    snapshot = getSnapshot()  ──┼──► GameSnapshot (immutable)
│  }  ← lock released            │         │
│                                │    view.render(snapshot)
│  view.repaint()  ─────────────►│         │ paints freely,
│                                │         │ no lock held
```
The lock is held for two very short windows: the physics step, and the snapshot copy. The view never competes with the physics loop during painting.
​

___

## Petri Net Sketch (for Report)

For the report, model each ball with two places: **`Moving`** and **`Still`**.

- Transitions: `kick` (Still → Moving), `friction_stop` (Moving → Still), `hole_entry` (Moving → consumed)
- The game-level net has a **`Playing`** place that transitions to **`GameOver`** when the ball-count place empties or when a player ball enters a hole

---

## Implementation Tips

- Use `CopyOnWriteArrayList` or a synchronized snapshot for the view to read without blocking the physics loop
- The bot "AI" can simply be a thread sleeping for a random interval then applying a random `(dx, dy)` impulse to its ball — the assignment does not require smart behavior
- For JPF verification, focus on the monitor invariants of `GameModel` (no two threads updating scores simultaneously) — that's a realistic and manageable scope for model checking
- For performance tests, compare sequential step time vs. parallel step time as the number of balls scales from 100 to 10,000

---

## Quick Tips
- Use CopyOnWriteArrayList or a synchronized snapshot for the view to read without blocking the physics loop

- The bot "AI" can simply be a thread sleeping for a random interval then applying a random (dx, dy) impulse to its ball — the assignment says it doesn't need to be smart

- For JPF verification, focus on the monitor invariants of GameModel (no two threads updating scores simultaneously) — that's a realistic scope for model checking
