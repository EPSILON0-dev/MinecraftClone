package com.ee;

import org.joml.*;
import org.junit.jupiter.api.Test;

import com.ee.Client.PhysicsObject;
import com.ee.Client.ClientWorld;
import com.ee.Common.Block;
import com.ee.Common.BlockType;
import com.ee.Common.Config;
import com.ee.Common.Chunk;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PhysicsObjectTest {

    @Test
    public void setDirectionAlwaysNormalizesTheVector() {
        PhysicsObject object = new PhysicsObject(
                new Vector3f(0.0f, 2.0f, 0.0f),
                new Vector3f(10.0f, 0.0f, 0.0f),
                0.3f,
                1.8f,
                10.0f,
                9.81f);

        object.setDirection(new Vector3f(0.0f, 0.0f, 4.0f));

        assertEquals(1.0f, object.direction().length(), 0.0001f);
        assertEquals(0.0f, object.direction().x, 0.0001f);
        assertEquals(0.0f, object.direction().y, 0.0001f);
        assertEquals(1.0f, object.direction().z, 0.0001f);
    }

    @Test
    public void updateAppliesFrictionBeforeMovementAndGravityWhileAirborne() {
        ClientWorld world = createEmptyWorld();
        PhysicsObject object = new PhysicsObject(
                new Vector3f(2.0f, 5.0f, 2.0f),
                new Vector3f(1.0f, 0.0f, 0.0f),
                0.3f,
                1.8f,
                10.0f,
                9.81f);

        object.setVelocity(new Vector3f(2.0f, 0.0f, 0.0f));
        object.update(world, 0.1f);

        assertEquals(0.0f, object.velocity().x, 0.0001f);
        assertEquals(-0.981f, object.velocity().y, 0.0001f);
        assertEquals(2.0f, object.position().x, 0.0001f);
        assertEquals(4.9019f, object.position().y, 0.0001f);
        assertEquals(2.0f, object.position().z, 0.0001f);
    }

    @Test
    public void updateCancelsGravityWhenObjectStartsOnGround() {
        ClientWorld world = createFloorWorld();
        PhysicsObject object = new PhysicsObject(
                new Vector3f(0.5f, 1.0f, 0.5f),
                new Vector3f(1.0f, 0.0f, 0.0f),
                0.3f,
                1.8f,
                10.0f,
                9.81f);

        object.update(world, 0.1f);

        assertEquals(0.0f, object.velocity().y, 0.0001f);
        assertEquals(1.0f, object.position().y, 0.0001f);
    }

    @Test
    public void getRightVectorUsesDirectionCrossWorldUp() {
        PhysicsObject object = new PhysicsObject(
                new Vector3f(0.0f, 2.0f, 0.0f),
                new Vector3f(1.0f, 0.0f, 0.0f),
                0.3f,
                1.8f,
                10.0f,
                9.81f);

        Vector3f right = object.getRightVector();

        assertEquals(0.0f, right.x, 0.0001f);
        assertEquals(0.0f, right.y, 0.0001f);
        assertEquals(1.0f, right.z, 0.0001f);
    }

    private static ClientWorld createEmptyWorld() {
        ClientWorld world = new ClientWorld();
        world.addChunk(new Vector2i(0, 0), new Chunk(new Vector2i(0, 0)));
        fillWorld(world, BlockType.Air);
        return world;
    }

    private static ClientWorld createFloorWorld() {
        ClientWorld world = createEmptyWorld();
        for (int x = 0; x < Config.CHUNK_SIZE.x; x++) {
            for (int z = 0; z < Config.CHUNK_SIZE.z; z++) {
                world.setBlock(new Vector3i(x, 0, z), new Block(BlockType.Cobblestone));
            }
        }
        return world;
    }

    private static void fillWorld(ClientWorld world, BlockType blockType) {
        for (int x = 0; x < Config.CHUNK_SIZE.x; x++) {
            for (int y = 0; y < Config.CHUNK_SIZE.y; y++) {
                for (int z = 0; z < Config.CHUNK_SIZE.z; z++) {
                    world.setBlock(new Vector3i(x, y, z), new Block(blockType));
                }
            }
        }
    }
}
