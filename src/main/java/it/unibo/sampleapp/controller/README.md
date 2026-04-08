# Controller Package

## Goal
This package coordinates input, model updates, and lifecycle orchestration.
It is the bridge between view events and model operations.

## What Is Inside
- `Controller`: controller API consumed by the view.
- `ControllerImpl`: concrete coordinator for game startup, input handling, and shutdown.
- `concurrent/multithread/*`: thread-based loop and bot workers.
- `concurrent/taskbased/*`: executor-based loop and bot workers.

## Organization
- **Input boundary**: `ViewImpl` calls `Controller` methods (`onDirectionInput`, `onShoot`, `onAim`).
- **Model command routing**: `ControllerImpl` converts UI actions into model method calls.
- **Loop orchestration**: controller chooses and starts multithread or task-based mode.
- **Game-over handling**: controller watches model status and triggers view game-over display.

## Specific Responsibility in MVC
- Receive user intents from View.
- Apply state-changing commands to Model.
- Retrieve/update rendering flow through game loop components.
- Manage runtime lifecycle (start/stop threads/tasks).

