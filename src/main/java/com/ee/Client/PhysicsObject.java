package com.ee.Client;

import org.joml.*;

import com.ee.Common.Config;
import com.ee.Common.Physics;

public class PhysicsObject {
    protected Vector3f position;
    protected Vector3f direction;
    protected Vector3f velocity;
    protected float colliderRadius;
    protected float colliderHeight;
    protected float frictionCoefficient = 10.0f;
    protected float gravityStrength = 9.81f;

    public PhysicsObject(Vector3f position, Vector3f direction, float colliderRadius, float colliderHeight,
            float frictionCoefficient, float gravityStrength) {
        this.position = position;
        this.direction = new Vector3f(direction).normalize();
        this.velocity = new Vector3f(0);
        this.colliderRadius = colliderRadius;
        this.colliderHeight = colliderHeight;
        this.frictionCoefficient = frictionCoefficient;
        this.gravityStrength = gravityStrength;
    }

    public Vector3f position() {
        return position;
    }

    public Vector3f direction() {
        return direction;
    }

    public Vector3f velocity() {
        return velocity;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public void setDirection(Vector3f direction) {
        this.direction = new Vector3f(direction).normalize();
    }

    public void setVelocity(Vector3f velocity) {
        this.velocity = velocity;
    }

    public void update(ClientWorld world, float deltaTime) {
        applyFriction(deltaTime);
        applyGravity(deltaTime);
        checkOutOfWorld();
        cancelGravityOnGround(world);
        Vector3f newPosition = new Vector3f(position).add(new Vector3f(velocity).mul(deltaTime));
        Vector3f resolvedPosition = Physics.resolveCapsuleCollision(world, newPosition, colliderRadius, colliderHeight);
        position.set(resolvedPosition);
    }

    public void addForce(Vector3f force) {
        velocity.add(force);
    }

    public Vector3f getRightVector() {
        Vector3f dest = new Vector3f();
        direction.cross(new Vector3f(0, 1, 0), dest).normalize();
        return dest;
    }

    public Vector3f getForwardVector() {
        Vector3f dest = new Vector3f();
        dest.set(direction).normalize();
        return dest;
    }

    protected boolean isOnGround(ClientWorld world) {
        Vector3f feetPosition = new Vector3f(position).add(0.0f, -0.01f, 0.0f);
        Vector3f resolvedFeetPosition = Physics.resolveCapsuleCollision(world, feetPosition, colliderRadius,
                colliderHeight);
        return resolvedFeetPosition.y > feetPosition.y + Config.PHYSICS_EPSILON;
    }

    private void applyFriction(float deltaTime) {
        Vector3f friction = new Vector3f(velocity).mul(-frictionCoefficient * deltaTime);
        friction.y = 0.0f; // Don't apply friction vertically
        addForce(friction);
    }

    private void applyGravity(float deltaTime) {
        addForce(new Vector3f(0, -gravityStrength * deltaTime, 0));
    }

    private void cancelGravityOnGround(ClientWorld world) {
        if (isOnGround(world)) {
            if (velocity.y < 0.0f) {
                velocity.y = 0.0f;
            }
        }
    }

    private void checkOutOfWorld() {
        if (position.y < Config.WORLD_OUT_OF_BOUNDS_Y) {
            position.set(0, 130, 0);
        }
    }
}
