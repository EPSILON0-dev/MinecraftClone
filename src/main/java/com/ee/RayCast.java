package com.ee;

import java.util.Optional;
import java.lang.Math;

import org.joml.*;

public class RayCast {
    public static Optional<Vector3i> rayCast(Camera camera, World world, float maxDistance, boolean previous) {
        if (maxDistance <= 0.0f) {
            return Optional.empty();
        }

        Vector3f origin = new Vector3f(camera.position());
        Vector3f direction = new Vector3f(camera.direction());
        if (direction.lengthSquared() < Config.RAYCAST_EPSILON) {
            return Optional.empty();
        }
        direction.normalize();

        Vector3i currentBlock = new Vector3i(
                (int) Math.floor(origin.x),
                (int) Math.floor(origin.y),
                (int) Math.floor(origin.z));
        Vector3i previousBlock = new Vector3i(currentBlock);

        if (world.getBlockNoThrow(currentBlock).type != BlockType.Air) {
            return Optional.of(new Vector3i(currentBlock));
        }

        int stepX = direction.x > 0.0f ? 1 : direction.x < 0.0f ? -1 : 0;
        int stepY = direction.y > 0.0f ? 1 : direction.y < 0.0f ? -1 : 0;
        int stepZ = direction.z > 0.0f ? 1 : direction.z < 0.0f ? -1 : 0;

        float tMaxX = initialAxisDistance(origin.x, currentBlock.x, direction.x, stepX);
        float tMaxY = initialAxisDistance(origin.y, currentBlock.y, direction.y, stepY);
        float tMaxZ = initialAxisDistance(origin.z, currentBlock.z, direction.z, stepZ);

        float tDeltaX = axisStepDistance(direction.x);
        float tDeltaY = axisStepDistance(direction.y);
        float tDeltaZ = axisStepDistance(direction.z);

        float travelled = 0.0f;
        while (travelled <= maxDistance) {
            if (tMaxX <= tMaxY && tMaxX <= tMaxZ) {
                currentBlock.x += stepX;
                travelled = tMaxX;
                tMaxX += tDeltaX;
            } else if (tMaxY <= tMaxZ) {
                currentBlock.y += stepY;
                travelled = tMaxY;
                tMaxY += tDeltaY;
            } else {
                currentBlock.z += stepZ;
                travelled = tMaxZ;
                tMaxZ += tDeltaZ;
            }

            if (travelled > maxDistance) {
                break;
            }

            if (world.getBlockNoThrow(currentBlock).type != BlockType.Air) {
                if (previous) {
                    return Optional.of(new Vector3i(previousBlock));
                }
                else {
                    return Optional.of(new Vector3i(currentBlock));
                }
            }
            previousBlock.set(currentBlock);
        }

        return Optional.empty();
    }

    private static float initialAxisDistance(float originComponent, int cell, float directionComponent, int step) {
        if (step == 0) {
            return Float.POSITIVE_INFINITY;
        }

        float nextBoundary = step > 0 ? cell + 1.0f : cell;
        return (nextBoundary - originComponent) / directionComponent;
    }

    private static float axisStepDistance(float directionComponent) {
        if (Math.abs(directionComponent) < Config.RAYCAST_EPSILON) {
            return Float.POSITIVE_INFINITY;
        }

        return Math.abs(1.0f / directionComponent);
    }
}
