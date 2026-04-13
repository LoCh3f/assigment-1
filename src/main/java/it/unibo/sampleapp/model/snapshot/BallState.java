package it.unibo.sampleapp.model.snapshot;

import it.unibo.sampleapp.model.ball.Ball;

public record BallState(int id, double px, double py, double vx, double vy, double radius,Ball.Type lastHit, Ball.Type type) {}