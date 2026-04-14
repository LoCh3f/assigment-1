# Poool Game

**PCD a.y. 2024-2025 — ISI LM UNIBO — Cesena Campus**

A concurrent implementation of a pool-like game where a human player and a bot compete to pocket small balls using physics simulation. The game features two concurrency models: multithreaded (using `Thread` instances) and task-based (using Java's `Executor` framework).

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

## Package Structure

```
src/main/java/it/unibo/sampleapp/
├── Main.java                    # Application entry point
├── controller/
│   ├── Controller.java          # Controller interface
│   ├── ControllerImpl.java      # Controller implementation
│   ├── README.md
│   └── concurrent/
│       ├── BotAIConstants.java
│       ├── GameLoopConstants.java
│       ├── multithread/         # Thread-based concurrency
│       │   ├── BotThread.java   # Bot AI thread
│       │   └── GameLoopThread.java  # Physics loop thread
│       └── taskbased/           # Executor-based concurrency
│           ├── BotTask.java     # Bot AI task
│           └── GameLoopTask.java    # Physics loop task
├── model/
│   ├── Model.java               # Model interface
│   ├── GameModel.java           # Model implementation (monitor)
│   ├── README.md
│   ├── ball/
│   │   ├── Ball.java            # Ball interface
│   │   └── impl/
│   │       └── ImplBall.java    # Ball implementation
│   ├── constants/
│   │   └── GameModelConstants.java
│   ├── hole/
│   │   ├── Hole.java            # Hole interface
│   │   └── impl/
│   │       └── HoleImpl.java    # Hole implementation
│   ├── snapshot/
│   │   ├── BallSnapshot.java    # Immutable ball snapshot
│   │   └── GameSnapshot.java    # Immutable game snapshot
│   ├── physics/
│   │   ├── PhysicsEngine.java   # Physics simulation
│   │   └── concurrent/
│   │       ├── CollisionBag.java
│   │       └── CollisionWorker.java
│   └── status/
│       └── GameStatus.java      # Game status enum
├── util/
│   └── Vector2D.java            # 2D vector utilities
└── view/
    ├── README.md
    ├── View.java                # View interface
    ├── ViewImpl.java            # Swing view implementation
    ├── board/
    │   └── BoardPanel.java      # Board rendering panel
    └── constants/
        ├── BoardPanelConstants.java
        └── ViewConstants.java
```

## Overview

Poool is a simplified pool game with the following elements:
- **Board**: 1920x1080 virtual playing area with walls and holes.
- **Balls**: configurable small balls (default `1000`), 1 human-controlled ball (blue), 1 bot-controlled ball (red).
- **Objective**: Human and bot compete to pocket as many small balls as possible.
- **Asynchronous Gameplay**: Human and bot act concurrently.
- **Physics**: Realistic ball movement with friction, elastic collisions, and wall bounces.
- **Concurrency**: Separate threads/tasks for physics simulation, bot AI, and user input.

## Controls

- **Arrow Keys**: Apply impulse to the human ball (up, down, left, right).
- **Mouse**: Click and drag to aim and shoot with power scaling.
- **Gameplay**: Human and bot play asynchronously.

## Architecture

The application follows an **MVC (Model-View-Controller)** pattern with dedicated active components for concurrency:

### MVC Dependency UML

```text
+------+   input   +------------+   state change   +-------+
| View | --------> | Controller | ---------------> | Model |
+------+           +------------+                  +-------+
   ^                      |                            |
   |      update(snapshot)|                            | getSnapshot()
   +----------------------+----------------------------+
```

### Key Components

- **Model**: `GameModel` is the synchronized monitor for shared game state.
- **View**: Swing UI that renders immutable `GameSnapshot` instances.
- **Controller**: Maps user input to model commands and drives view updates.
- **Physics**: `PhysicsEngine` handles movement, friction, borders, collisions, and holes.
- **Concurrency**: game loop and bot are available in both thread-based and task-based modes.

## Concurrency Modes

### Multithreaded Version (Default)
- `GameLoopThread`: Runs the physics simulation at ~60 FPS using a `Thread`.
- `BotThread`: Bot AI using a dedicated `Thread`.

### Task-Based Version
- `GameLoopTask`: Physics simulation using `ScheduledExecutorService` for periodic execution.
- `BotTask`: Bot AI using `ExecutorService`, resubmitting itself after each move.

Both versions ensure the `GameModel` monitor is used correctly, with `wait()`/`notifyAll()` for synchronization on game-over conditions.

## Testing

Run unit tests:
```bash
./gradlew test
```

Tests cover ball physics, collisions, model synchronization, and utility classes.

## Key Design Decisions

- **Monitor Pattern**: `GameModel` uses `synchronized` methods and `wait()`/`notifyAll()` for thread coordination.
- **Snapshot Rendering**: View reads immutable copies of game state to prevent data races.
- **Asynchronous Input**: User input is handled on Swing EDT, decoupled from physics loop.
- **Event-Driven Bot**: Bot decisions are decoupled from UI events.

## Performance Notes

- Physics loop targets 60 FPS with sleep-based timing.
- Task-based version uses `ScheduledExecutorService` for consistent timing.
- Ball movement and friction are computed each frame; collision resolution is synchronized through the model monitor.

## License

This project is part of the PCD course assignment at University of Bologna.
