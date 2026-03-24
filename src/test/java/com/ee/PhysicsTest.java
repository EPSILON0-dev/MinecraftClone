package com.ee;

import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PhysicsTest {

    @Test
    public void resolveCapsuleCollisionLeavesFreeCapsuleUnchanged() {
        World world = createEmptyWorld();
        Vector3f position = new Vector3f(4.5f, 4.0f, 4.5f);

        Vector3f resolved = Physics.resolveCapsuleCollision(world, position, 0.3f, 1.8f);

        assertEquals(position.x, resolved.x, 0.0001f);
        assertEquals(position.y, resolved.y, 0.0001f);
        assertEquals(position.z, resolved.z, 0.0001f);
    }

    @Test
    public void resolveCapsuleCollisionPushesOutOfSolidBlockHorizontally() {
        World world = createEmptyWorld();
        world.setBlock(new Vector3i(0, 0, 0), new Block(BlockType.Cobblestone));

        Vector3f resolved = Physics.resolveCapsuleCollision(world, new Vector3f(0.2f, 0.6f, 0.5f), 0.3f, 1.8f);

        assertTrue(resolved.x < 0.0f);
        assertEquals(0.6f, resolved.y, 0.0001f);
        assertEquals(0.5f, resolved.z, 0.0001f);
    }

    @Test
    public void isOnGroundDetectsFloorAndAirborneState() {
        World world = createFloorWorld();

        assertTrue(Physics.isOnGround(world, new Vector3f(0.5f, 1.0f, 0.5f), 0.3f, 1.8f));
        assertFalse(Physics.isOnGround(world, new Vector3f(0.5f, 3.0f, 0.5f), 0.3f, 1.8f));
    }

    @Test
    public void canPlaceBlockAtRejectsBlockOverlappingCapsule() {
        assertFalse(Physics.canPlaceBlockAt(new Vector3f(0.5f, 1.0f, 0.5f), new Vector3i(0, 1, 0), 0.3f, 1.8f));
    }

    @Test
    public void canPlaceBlockAtAllowsBlockThatOnlyTouchesCapsuleBoundary() {
        assertTrue(Physics.canPlaceBlockAt(new Vector3f(0.5f, 1.0f, 0.5f), new Vector3i(0, 0, 0), 0.3f, 1.8f));
    }

    private static World createEmptyWorld() {
        World world = new World(new Vector2i(1, 1), false);
        fillWorld(world, BlockType.Air);
        return world;
    }

    private static World createFloorWorld() {
        World world = createEmptyWorld();
        for (int x = 0; x < Config.CHUNK_SIZE.x; x++) {
            for (int z = 0; z < Config.CHUNK_SIZE.z; z++) {
                world.setBlock(new Vector3i(x, 0, z), new Block(BlockType.Cobblestone));
            }
        }
        return world;
    }

    private static void fillWorld(World world, BlockType blockType) {
        for (int x = 0; x < Config.CHUNK_SIZE.x; x++) {
            for (int y = 0; y < Config.CHUNK_SIZE.y; y++) {
                for (int z = 0; z < Config.CHUNK_SIZE.z; z++) {
                    world.setBlock(new Vector3i(x, y, z), new Block(blockType));
                }
            }
        }
    }
}
