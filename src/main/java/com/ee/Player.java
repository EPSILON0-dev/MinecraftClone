package com.ee;

import org.joml.*;
import java.lang.Math;

public class Player extends PhysicsObject {
    private float yaw;
    private float pitch;
    private float currentFov;
    private boolean isSprinting;

    public Player(Vector3f position, Vector3f direction) {
        super(position, direction, Config.PLAYER_COLLIDER_RADIUS, Config.PLAYER_COLLIDER_HEIGHT, Config.PLAYER_FRICTION,
                Config.PLAYER_GRAVITY);
        syncAnglesFromDirection();
        currentFov = Config.CAMERA_NORMAL_FOV;
    }

    public void setupCamera(Camera camera) {
        Vector3f cameraPosition = new Vector3f(position).add(0, Config.PLAYER_CAMERA_HEIGHT_OFFSET, 0);
        camera.setPosition(cameraPosition);
        camera.setDirection(direction);
        camera.setFov(currentFov);
    }

    public void rotate(float yawDelta, float pitchDelta) {
        yaw += yawDelta;
        pitch = Math.clamp(pitch + pitchDelta, -Config.CAMERA_MAX_PITCH, Config.CAMERA_MAX_PITCH);
        updateDirectionFromAngles();
    }

    public void setSprinting(boolean sprinting) {
        isSprinting = sprinting;
    }

    public void move(World world, Vector2f vector) {
        addMovementForce(world, getRightVector().mul(vector.x).add(getForwardVector().mul(vector.y)));
    }

    public void jump(World world) {
        if (Physics.isOnGround(world, position, colliderRadius, colliderHeight)) {
            velocity = velocity.add(0, Config.PLAYER_JUMP_IMPULSE, 0);
            System.out.println("Jumped");
        }
    }

    @Override
    public void update(World world, float deltaTime) {
        super.update(world, deltaTime);

        // Smoothly interpolate FOV based on sprinting state
        float targetFov = isSprinting ? Config.CAMERA_NORMAL_FOV * Config.PLAYER_SPRINT_FOV_MULTIPLIER
                : Config.CAMERA_NORMAL_FOV;
        currentFov = Util.lerp(currentFov, targetFov,
                1.0f - (float) Math.exp(-Config.CAMERA_FOV_LERP_SPEED * deltaTime));
    }

    public boolean canPlaceBlockAt(Vector3i blockPos) {
        return Physics.canPlaceBlockAt(position, blockPos, colliderRadius, colliderHeight);
    }

    public void dumpDebugInfo(World world) {
        System.out.print("Position: " + position);
        System.out.print(", Direction: " + direction);
        System.out.print(", Velocity: " + velocity);
        System.out.println(isOnGround(world) ? ", On Ground" : ", In Air");
    }

    private void addMovementForce(World world, Vector3f movementDirection) {
        movementDirection.y = 0.0f;
        boolean onGround = Physics.isOnGround(world, position, colliderRadius, colliderHeight);
        float speed = Config.PLAYER_MOVEMENT_SPEED * (onGround ? 1.0f : Config.PLAYER_AIR_CONTROL_MULTIPLIER)
                * (isSprinting ? Config.PLAYER_SPRINT_MULTIPLIER : 1.0f);
        addForce(new Vector3f(movementDirection).normalize().mul(speed));
    }

    private void syncAnglesFromDirection() {
        yaw = (float) Math.atan2(direction.z, direction.x);
        pitch = (float) Math.asin(direction.y);
    }

    private void updateDirectionFromAngles() {
        direction.set(
                (float) (Math.cos(yaw) * Math.cos(pitch)),
                (float) Math.sin(pitch),
                (float) (Math.sin(yaw) * Math.cos(pitch))).normalize();
    }
}
