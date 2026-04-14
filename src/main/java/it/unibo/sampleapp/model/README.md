# Model Package

## Goal
This package contains the domain state and rules of the game.
It is the single source of truth for gameplay state and physics progression.

## What Is Inside
- `Model`: interface that defines the model contract used by controller and game loops.
- `GameModel`: synchronized monitor implementation of `Model`.
- `MultithreadGameModel` / `TaskBasedGameModel`: specializations that wire the collision strategy.
- `physics/PhysicsEngine`: pure physics step logic (movement, collisions, holes).
- `domain/ball/*`: ball abstraction and concrete implementation.
- `domain/hole/*`: hole abstraction and concrete implementation.
- `snapshot/*`: immutable DTOs used by the view (`GameSnapshot`, `BallSnapshot`).
- `status/GameStatus`: game lifecycle states (playing, wins, draw).

## Organization
- **Contract vs implementation**: `Model` is the API, `GameModel` is the implementation.
- **State ownership**: only `GameModel` owns mutable game state.
- **Read model for rendering**: snapshots are immutable and safe to share across threads.
- **Physics isolation**: `PhysicsEngine` contains simulation details, while `GameModel` handles scoring/win rules.
- **Strategy wiring**: collision resolution is injected into `PhysicsEngine` by the concrete game-model specialization.

## Specific Responsibility in MVC
- Accept commands from controller (`applyImpulseToHuman`, bot impulses, physics ticks).
- Produce immutable snapshots with `getSnapshot()` for rendering.
- Enforce game rules and publish current status with `getStatus()`.

