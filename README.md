# Poool Game

**PCD a.y. 2024-2025 — ISI LM UNIBO — Cesena Campus**

A concurrent implementation of a pool-like game where a human player and a bot compete to pocket small balls using physics simulation. The game features two concurrency models: multithreaded (using `Thread` instances) and task-based (using Java's `Executor` framework).

## Overview

Poool is a simplified pool game with the following elements:
- **Board**: 900x600 pixel playing area with walls and holes.
- **Balls**: 500 small balls, 1 human-controlled ball (blue), 1 bot-controlled ball (red).
- **Objective**: Human and bot compete to pocket as many small balls as possible.
- **Asynchronous Gameplay**: Players can move whenever all balls are stopped.
- **Physics**: Realistic ball movement with friction, elastic collisions, and wall bounces.
- **Concurrency**: Asynchronous gameplay with separate threads/tasks for physics simulation, bot AI, and user input.

## Architecture

The application follows an **MVC (Model-View-Controller)** pattern with dedicated active components for concurrency:

```
┌─────────────────────────────────────────────┐
│                  View (Swing/AWT)           │  ← Passive renderer
│                                             │
│  ← reads snapshot                          │
└─────────────────────┬───────────────────────┘
                      │
┌─────────────────────▼───────────────────────┐
│              Controller                      │  ← Handles input, coordinates
│                                             │
│  updates →                                  │
└─────────────────────┬───────────────────────┘
                      │
┌─────────────────────▼───────────────────────┐
│              GameModel (Monitor)             │  ← Shared mutable state
│  - List<Ball> balls                          │
│  - int humanScore, botScore                  │
│  - GameStatus status                         │
│  + synchronized getters/setters              │
└───┬───────────────┬───────────────┬──────────┘
│               │               │
┌───▼────┐  ┌───────▼──────┐  ┌────▼──────────┐
│Physics │  │  BotAgent    │  │ Input Relay   │
│Loop    │  │  (async)     │  │ (Swing EDT)   │
│Thread/ │  │  Thread/Task │  │ (Event-driven)│
│Task    │  │              │  │                │
└────────┘  └──────────────┘  └───────────────┘
```

### MVC Flow
- **Model**: `GameModel` holds game state and acts as a monitor.
- **View**: Displays game state via immutable snapshots, receives updates from Controller.
- **Controller**: Processes user input, triggers model updates, and pushes view updates.
- **Active Components**: Physics loop and bot AI run asynchronously, updating the model and triggering view refreshes.

### Key Components

- **Model**: `GameModel` acts as a monitor with synchronized methods for thread-safe access to game state.
- **View**: Swing-based GUI displaying the board, balls, scores, and FPS. Uses immutable snapshots to avoid blocking the physics loop.
- **Controller**: Handles user input and coordinates between model and view.
- **Physics**: Simulates ball movement, collisions, friction, and hole detection.
- **Concurrency**: Two implementations for the physics loop and bot AI.

## Concurrency Modes

### Multithreaded Version (Default)
- `GameLoopThread`: Runs the physics simulation at ~60 FPS using a `Thread`.
- `BotThread`: Asynchronous bot AI using a `Thread` that waits for balls to stop before making moves.

### Task-Based Version
- `GameLoopTask`: Physics simulation using `ScheduledExecutorService` for periodic execution.
- `BotTask`: Bot AI using `ExecutorService`, resubmitting itself after each move.

Both versions ensure the `GameModel` monitor is used correctly, with `wait()`/`notifyAll()` for synchronization on game-over conditions.

## Build Instructions

### Prerequisites
- Java 21 or later
- Gradle 8.0 or later (wrapper included)

### Build
```bash
./gradlew build
```

This compiles the code, runs tests, and generates JAR files in `build/libs/`.

### Shadow JAR
To create a runnable JAR with all dependencies:
```bash
./gradlew shadowJar
```

The JAR file `assignment-1-all.jar` will be in `build/libs/`.

## Run Instructions

### Using Gradle
- **Multithreaded version** (default):
  ```bash
  ./gradlew run
  ```
- **Task-based version**:
  ```bash
  ./gradlew run --args="taskbased"
  ```

### Using JAR
- **Multithreaded version**:
  ```bash
  java -jar build/libs/assignment-1-all.jar
  ```
- **Task-based version**:
  ```bash
  java -jar build/libs/assignment-1-all.jar taskbased
  ```

The window title will display "Pool - MULTITHREAD" or "Pool - TASKBASED" to indicate the active mode.

## Controls

- **Arrow Keys**: Apply impulse to human ball (up, down, left, right) - only works when all balls are stopped.
- **Mouse**: Click and drag to aim and shoot with power based on hold time - only works when all balls are stopped.
- **Gameplay**: Asynchronous play - both players can move whenever all balls are stopped.

## Testing

Run unit tests:
```bash
./gradlew test
```

Tests cover ball physics, collisions, model synchronization, and utility classes.

## Package Structure

```
src/main/java/it/unibo/sampleapp/
├── Main.java                    # Application entry point
├── controller/
│   ├── Controller.java          # Controller interface
│   └── ControllerImpl.java      # Controller implementation
│   └── concurrent/
│       ├── multithread/         # Thread-based concurrency
│       │   ├── BotThread.java   # Bot AI thread
│       │   └── GameLoopThread.java  # Physics loop thread
│       └── taskbased/           # Executor-based concurrency
│           ├── BotTask.java     # Bot AI task
│           └── GameLoopTask.java    # Physics loop task
├── model/
│   ├── GameModel.java           # Model interface
│   ├── GameModelImpl.java       # Model implementation (monitor)
│   ├── ball/
│   │   ├── Ball.java            # Ball interface
│   │   └── impl/
│   │       └── ImplBall.java    # Ball implementation
│   ├── hole/
│   │   ├── Hole.java            # Hole interface
│   │   └── impl/
│   │       └── HoleImpl.java    # Hole implementation
│   ├── snapshot/
│   │   ├── BallSnapshot.java    # Immutable ball snapshot
│   │   └── GameSnapshot.java    # Immutable game snapshot
│   └── status/
│       └── GameStatus.java      # Game status enum
├── util/
│   ├── Vector2D.java            # 2D vector utilities
│   └── physics/
│       └── PhysicsEngine.java   # Physics simulation
├── view/
│   ├── View.java                # View interface
│   ├── ViewImpl.java            # Swing view implementation
│   └── board/
│       └── BoardPanel.java      # Board rendering panel
```

## Key Design Decisions

- **Monitor Pattern**: `GameModel` uses `synchronized` methods and `wait()`/`notifyAll()` for thread coordination.
- **Snapshot Rendering**: View reads immutable copies of game state to prevent data races.
- **Asynchronous Input**: User input is handled on Swing EDT, decoupled from physics loop.
- **Event-Driven Bot**: Bot waits for balls to stop before acting, ensuring fair play.

## Performance Notes

- Physics loop targets 60 FPS with sleep-based timing.
- Task-based version uses `ScheduledExecutorService` for consistent timing.
- Ball movement and friction are computed sequentially; collisions are resolved in a critical section.

## License

This project is part of the PCD course assignment at University of Bologna.
