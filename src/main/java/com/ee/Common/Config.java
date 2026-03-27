package com.ee.Common;

import org.joml.*;
import java.lang.Math;

public class Config {
    public static final int BLOCK_DATA_SIZE = 1; // In bytes, for serialization
    public static final Vector3i CHUNK_SIZE = new Vector3i(16, 128, 16);
    public static final int CHUNK_BLOCK_COUNT = CHUNK_SIZE.x * CHUNK_SIZE.y * CHUNK_SIZE.z;
    public static final float PHYSICS_EPSILON = 1.0e-3f;
    public static final float RAYCAST_EPSILON = 1.0e-6f;
    public static final float CAMERA_MAX_PITCH = (float) Math.toRadians(89.0f);
    public static final float CAMERA_NORMAL_FOV = 90.0f;
    public static final float CAMERA_FOV_LERP_SPEED = 10.0f;
    public static final float PLAYER_COLLIDER_RADIUS = 0.3f;
    public static final float PLAYER_COLLIDER_HEIGHT = 1.8f;
    public static final float PLAYER_MOVEMENT_SPEED = 2.0f;
    public static final float PLAYER_CAMERA_HEIGHT_OFFSET = 1.6f;
    public static final float PLAYER_FRICTION = 20.0f;
    public static final float PLAYER_GRAVITY = 9.81f * 2.0f; // For better feel :p
    public static final float PLAYER_JUMP_IMPULSE = 7.0f;
    public static final float PLAYER_AIR_CONTROL_MULTIPLIER = 1.0f;
    public static final float PLAYER_SPRINT_MULTIPLIER = 1.5f;
    public static final float PLAYER_SPRINT_FOV_MULTIPLIER = 1.2f;
    public static final int WORLD_MAX_SIZE_CHUNKS = 8192; // On each axis
    public static final int WORLD_CHUNK_DISTANCE = 3;
    public static final float WORLD_OUT_OF_BOUNDS_Y = -100.0f;
    public static final int RENDERER_MAX_MESHES_PER_FRAME = 4;
}
