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
```text
src/main/java/it/unibo/sampleapp/
├── Main.java                    # Application entry point
├── controller/
│   ├── Controller.java          # Controller interface
│   ├── AbstractController.java  # Shared controller logic
│   ├── MultithreadController.java
│   ├── README.md
│   ├── bot/
│   │   ├── BotAIConstants.java
│   │   ├── BotDecisionService.java
│   │   └── BotMoveService.java
│   └── concurrent/
│       ├── GameLoopConstants.java
│       ├── multithread/         # Thread-based concurrency
│       │   ├── BotThread.java   # Bot AI thread
│       │   └── GameLoopThread.java  # Physics loop thread
│       └── taskbased/           # Executor-based concurrency helpers
│           └── TaskBasedController.java
├── model/
│   ├── Model.java               # Model interface
│   ├── GameModel.java           # Base synchronized monitor
│   ├── MultithreadGameModel.java # Thread-based model wiring
│   ├── TaskBasedGameModel.java   # Executor-based model wiring
│   ├── README.md
│   ├── constants/
│   │   └── GameModelConstants.java
│   ├── domain/
│   │   ├── ball/
│   │   │   ├── Ball.java            # Ball interface
│   │   │   └── impl/
│   │   │       └── ImplBall.java    # Ball implementation
│   │   └── hole/
│   │       ├── Hole.java            # Hole interface
│   │       └── impl/
│   │           └── HoleImpl.java    # Hole implementation
|   │   ├── physics/
|   │   │   ├── PhysicsEngine.java   # Physics simulation orchestrator
|   │   │   ├── collision/
|   │   │   │   ├── common/
|   │   │   │   │   ├── CollisionPairResolver.java
|   │   │   │   │   ├── CollisionPartitioning.java
|   │   │   │   │   └── CollisionResolverConstants.java
|   │   │   │   ├── multithread/
|   │   │   │   │   ├── CollisionBag.java
|   │   │   │   │   ├── CollisionWorker.java
|   │   │   │   │   └── ConcurrentCollisionResolver.java
|   │   │   │   ├── sequential/
|   │   │   │   │   └── SequentialCollisionResolver.java
|   │   │   │   └── taskbased/
|   │   │   │       └── TaskBasedCollisionResolver.java
|   │   │   └── step/
|   │   │       ├── PhysicsStepResolver.java  # Per-ball step abstraction
|   │   │       ├── common/
|   │   │       │   └── PhysicsStepPartitioning.java
|   │   │       ├── sequential/
|   │   │       │   └── SequentialPhysicsStepResolver.java
|   │   │       ├── multithread/
|   │   │       │   ├── PhysicsStepBag.java
|   │   │       │   ├── PhysicsStepWorker.java
|   │   │       │   └── ConcurrentPhysicsStepResolver.java
|   │   │       └── taskbased/
│   ├── snapshot/
│   │   ├── BallSnapshot.java    # Immutable ball snapshot
│   │   └── GameSnapshot.java    # Immutable game snapshot
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
- **Balls**: configurable small balls (default `500`), 1 human-controlled ball (blue), 1 bot-controlled ball (red).
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
- **Model**: `GameModel` is the synchronized monitor for shared game state; `MultithreadGameModel` and `TaskBasedGameModel` specialize the physics wiring.
- **View**: Swing UI that renders immutable `GameSnapshot` instances.
- **Controller**: Maps user input to model commands and drives view updates.
- **Controller**: `MultithreadController` and `TaskBasedController` specialize lifecycle orchestration.
- **Physics**: `PhysicsEngine` handles movement, friction, borders, collisions, and holes while delegating collision strategy to a pluggable resolver.
- **Concurrency**: game loop and bot are available in both thread-based and task-based modes.
## Concurrency Modes
### Multithreaded Version (Default)
- `GameLoopThread`: Runs the physics simulation at ~60 FPS using a `Thread`.
- `BotThread`: Bot AI using a dedicated `Thread`.
### Task-Based Version
- `TaskBasedController`: uses `ScheduledExecutorService` for the game loop and bot scheduling.
- `TaskBasedGameModel`: wires task-based collision resolution with an external `ExecutorService`.
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
- Ball movement and friction are computed each frame; collision resolution is synchronized through the model monitor and delegated to the selected collision strategy.
## License
This project is part of the PCD course assignment at University of Bologna.
