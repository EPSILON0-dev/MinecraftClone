package com.ee.Common;

import org.joml.*;

import com.ee.Client.ClientWorld;

import java.lang.Math;

public class Physics {
    public static Vector3f resolveCapsuleCollision(ClientWorld world, Vector3f position, float radius, float height) {
        Vector3f resolvedPosition = new Vector3f(position);
        final int maxIterations = 4;

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            boolean corrected = false;

            int minX = (int) Math.floor(resolvedPosition.x - radius);
            int maxX = (int) Math.floor(resolvedPosition.x + radius);
            int minY = (int) Math.floor(resolvedPosition.y);
            int maxY = (int) Math.floor(resolvedPosition.y + height);
            int minZ = (int) Math.floor(resolvedPosition.z - radius);
            int maxZ = (int) Math.floor(resolvedPosition.z + radius);

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        if (!world.isSolid(x, y, z)) {
                            continue;
                        }

                        Vector3f correction = capsuleBlockCorrection(resolvedPosition, radius, height, x, y, z);
                        if (correction.lengthSquared() == 0.0f) {
                            continue;
                        }

                        resolvedPosition.add(correction);
                        corrected = true;
                    }
                }
            }

            if (!corrected) {
                break;
            }
        }

        return resolvedPosition;
    }

    private static Vector3f capsuleBlockCorrection(Vector3f position, float radius, float height,
            int blockX, int blockY, int blockZ) {
        float boxMinX = blockX;
        float boxMaxX = blockX + 1.0f;
        float boxMinY = blockY;
        float boxMaxY = blockY + 1.0f;
        float boxMinZ = blockZ;
        float boxMaxZ = blockZ + 1.0f;

        float segmentStartY = position.y + radius;
        float segmentEndY = position.y + height - radius;

        if (segmentEndY < segmentStartY) {
            float midpointY = position.y + height * 0.5f;
            segmentStartY = midpointY;
            segmentEndY = midpointY;
        }

        float closestBoxX = Math.clamp(position.x, boxMinX, boxMaxX);
        float closestBoxZ = Math.clamp(position.z, boxMinZ, boxMaxZ);

        float closestSegmentY;
        float closestBoxY;
        if (segmentEndY < boxMinY) {
            closestSegmentY = segmentEndY;
            closestBoxY = boxMinY;
        } else if (segmentStartY > boxMaxY) {
            closestSegmentY = segmentStartY;
            closestBoxY = boxMaxY;
        } else {
            closestSegmentY = Math.clamp((segmentStartY + segmentEndY) * 0.5f, boxMinY, boxMaxY);
            closestBoxY = closestSegmentY;
        }

        Vector3f delta = new Vector3f(
                position.x - closestBoxX,
                closestSegmentY - closestBoxY,
                position.z - closestBoxZ);

        float distanceSquared = delta.lengthSquared();
        float radiusSquared = radius * radius;

        if (distanceSquared >= radiusSquared) {
            return new Vector3f(0.0f, 0.0f, 0.0f);
        }

        if (distanceSquared > 1.0e-6f) {
            float distance = (float) Math.sqrt(distanceSquared);
            float penetration = radius - distance + 1.0e-4f;
            return delta.div(distance).mul(penetration);
        }

        return axisAlignedFallbackCorrection(position, radius, boxMinX, boxMaxX, boxMinZ, boxMaxZ);
    }

    private static Vector3f axisAlignedFallbackCorrection(Vector3f position, float radius,
            float boxMinX, float boxMaxX, float boxMinZ, float boxMaxZ) {
        Vector3f bestCorrection = new Vector3f(0.0f, 0.0f, 0.0f);
        float smallestPenetration = Float.POSITIVE_INFINITY;
        float epsilon = 1.0e-4f;

        float pushNegativeX = radius - (position.x - boxMinX);
        if (pushNegativeX > 0.0f && pushNegativeX < smallestPenetration) {
            smallestPenetration = pushNegativeX;
            bestCorrection.set(-(pushNegativeX + epsilon), 0.0f, 0.0f);
        }

        float pushPositiveX = radius - (boxMaxX - position.x);
        if (pushPositiveX > 0.0f && pushPositiveX < smallestPenetration) {
            smallestPenetration = pushPositiveX;
            bestCorrection.set(pushPositiveX + epsilon, 0.0f, 0.0f);
        }

        float pushNegativeZ = radius - (position.z - boxMinZ);
        if (pushNegativeZ > 0.0f && pushNegativeZ < smallestPenetration) {
            smallestPenetration = pushNegativeZ;
            bestCorrection.set(0.0f, 0.0f, -(pushNegativeZ + epsilon));
        }

        float pushPositiveZ = radius - (boxMaxZ - position.z);
        if (pushPositiveZ > 0.0f && pushPositiveZ < smallestPenetration) {
            bestCorrection.set(0.0f, 0.0f, pushPositiveZ + epsilon);
        }

        return bestCorrection;
    }

    private static float capsuleBlockDistanceSquared(Vector3f position, float radius, float height,
            int blockX, int blockY, int blockZ) {
        float boxMinX = blockX;
        float boxMaxX = blockX + 1.0f;
        float boxMinY = blockY;
        float boxMaxY = blockY + 1.0f;
        float boxMinZ = blockZ;
        float boxMaxZ = blockZ + 1.0f;

        float segmentStartY = position.y + radius;
        float segmentEndY = position.y + height - radius;

        if (segmentEndY < segmentStartY) {
            float midpointY = position.y + height * 0.5f;
            segmentStartY = midpointY;
            segmentEndY = midpointY;
        }

        float capsuleCenterX = position.x;
        float capsuleCenterZ = position.z;
        float closestBoxX = Math.clamp(capsuleCenterX, boxMinX, boxMaxX);
        float closestBoxZ = Math.clamp(capsuleCenterZ, boxMinZ, boxMaxZ);

        float closestSegmentY;
        float closestBoxY;
        if (segmentEndY < boxMinY) {
            closestSegmentY = segmentEndY;
            closestBoxY = boxMinY;
        } else if (segmentStartY > boxMaxY) {
            closestSegmentY = segmentStartY;
            closestBoxY = boxMaxY;
        } else {
            closestSegmentY = Math.clamp((segmentStartY + segmentEndY) * 0.5f, boxMinY, boxMaxY);
            closestBoxY = closestSegmentY;
        }

        float deltaX = capsuleCenterX - closestBoxX;
        float deltaY = closestSegmentY - closestBoxY;
        float deltaZ = capsuleCenterZ - closestBoxZ;
        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
    }

    public static boolean isOnGround(ClientWorld world, Vector3f position, float radius, float height) {
        Vector3f feetPosition = new Vector3f(position).add(0.0f, -0.01f, 0.0f);
        Vector3f resolvedFeetPosition = resolveCapsuleCollision(world, feetPosition, radius, height);
        return resolvedFeetPosition.y > feetPosition.y + Config.PHYSICS_EPSILON;
    }

    public static boolean canPlaceBlockAt(Vector3f capsulePos, Vector3i blockPos, float radius, float height) {
        float distanceSquared = capsuleBlockDistanceSquared(capsulePos, radius, height,
                blockPos.x, blockPos.y, blockPos.z);
        float blockingRadius = Math.max(0.0f, radius - Config.PHYSICS_EPSILON);
        return distanceSquared >= blockingRadius * blockingRadius;
    }
}
