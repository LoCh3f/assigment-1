package it.unibo.sampleapp.model.snapshot;

import it.unibo.sampleapp.model.ball.Ball;

public record BallUpdate(int id, double newVx, double newVy, double newPx, double newPy, Ball.Type lastHit) {}