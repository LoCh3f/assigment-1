# View Package

## Goal
This package contains rendering and user interaction code.
It displays snapshots and forwards raw input to the controller.

## What Is Inside
- `View`: view contract used by the controller/game loop.
- `ViewImpl`: Swing window implementation and input event wiring.
- `board/BoardPanel`: custom panel that draws balls, holes, HUD, and overlays.

## Organization
- **Passive rendering**: view does not mutate model state directly.
- **Snapshot-driven UI**: rendering uses immutable `GameSnapshot` objects.
- **Input forwarding**: keyboard/mouse events are translated into `Controller` calls.
- **Visual state**: aiming helpers and overlays are maintained locally in view classes.

## Specific Responsibility in MVC
- Render current game state (`update(GameSnapshot)`).
- Show terminal state (`displayGameOver(GameStatus)`).
- Expose window lifecycle (`show()`, mode label updates).
- Keep model access indirect via controller only.

