package com.ee;

import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.junit.jupiter.api.Test;

import com.ee.Client.Camera;
import com.ee.Client.RayCast;
import com.ee.Client.ClientWorld;
import com.ee.Common.Block;
import com.ee.Common.BlockType;
import com.ee.Common.Chunk;
import com.ee.Common.Config;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RayCastTest {

    @Test
    public void rayCastReturnsFirstSolidBlockAlongCameraDirection() throws Exception {
        ClientWorld world = createWorld(new Vector3i(0, 64, 0), BlockType.Grass);
        Camera camera = new Camera(new Vector3f(0.5f, 65.5f, 0.5f), new Vector3f(0.0f, -1.0f, 0.0f),
                90.0f, 1.0f);

        Optional<Vector3i> hit = RayCast.rayCast(camera, world, 5.0f, false);

        assertTrue(hit.isPresent());
        assertEquals(new Vector3i(0, 64, 0), hit.get());
    }

    @Test
    public void rayCastReturnsPreviousAirBlockBeforeFirstSolidBlockWhenRequested() throws Exception {
        ClientWorld world = createWorld(new Vector3i(0, 64, 0), BlockType.Grass);
        Camera camera = new Camera(new Vector3f(0.5f, 65.5f, 0.5f), new Vector3f(0.0f, -1.0f, 0.0f),
                90.0f, 1.0f);

        Optional<Vector3i> hit = RayCast.rayCast(camera, world, 5.0f, true);

        assertTrue(hit.isPresent());
        assertEquals(new Vector3i(0, 65, 0), hit.get());
    }

    @Test
    public void rayCastReturnsCurrentBlockWhenCameraStartsInsideSolidBlock() throws Exception {
        ClientWorld world = createWorld(new Vector3i(2, 10, 3), BlockType.Cobblestone);
        Camera camera = new Camera(new Vector3f(2.5f, 10.5f, 3.5f), new Vector3f(1.0f, 0.0f, 0.0f),
                90.0f, 1.0f);

        Optional<Vector3i> hit = RayCast.rayCast(camera, world, 5.0f, false);

        assertTrue(hit.isPresent());
        assertEquals(new Vector3i(2, 10, 3), hit.get());
    }

    @Test
    public void rayCastReturnsCurrentBlockWhenPreviousRequestedAndCameraStartsInsideSolidBlock() throws Exception {
        ClientWorld world = createWorld(new Vector3i(2, 10, 3), BlockType.Cobblestone);
        Camera camera = new Camera(new Vector3f(2.5f, 10.5f, 3.5f), new Vector3f(1.0f, 0.0f, 0.0f),
                90.0f, 1.0f);

        Optional<Vector3i> hit = RayCast.rayCast(camera, world, 5.0f, true);

        assertTrue(hit.isPresent());
        assertEquals(new Vector3i(2, 10, 3), hit.get());
    }

    @Test
    public void rayCastReturnsEmptyWhenNoSolidBlockIsHit() throws Exception {
        ClientWorld world = createWorld(null, null);
        Camera camera = new Camera(new Vector3f(0.5f, 65.5f, 0.5f), new Vector3f(0.0f, 1.0f, 0.0f),
                90.0f, 1.0f);

        Optional<Vector3i> hit = RayCast.rayCast(camera, world, 5.0f, false);

        assertFalse(hit.isPresent());
    }

    @Test
    public void rayCastReturnsEmptyWhenPreviousRequestedAndNoSolidBlockIsHit() throws Exception {
        ClientWorld world = createWorld(null, null);
        Camera camera = new Camera(new Vector3f(0.5f, 65.5f, 0.5f), new Vector3f(0.0f, 1.0f, 0.0f),
                90.0f, 1.0f);

        Optional<Vector3i> hit = RayCast.rayCast(camera, world, 5.0f, true);

        assertFalse(hit.isPresent());
    }

    private static ClientWorld createWorld(Vector3i solidBlockPosition, BlockType solidBlockType) throws Exception {
        ClientWorld world = new ClientWorld();
        Chunk chunk = new Chunk(new Vector2i(0, 0));
        world.addChunk(new Vector2i(0, 0), chunk);
        fillChunk(chunk, BlockType.Air);
        if (solidBlockPosition != null && solidBlockType != null) {
            chunk.setBlock(solidBlockPosition, new Block(solidBlockType));
        }
        return world;
    }

    private static void fillChunk(Chunk chunk, BlockType blockType) {
        for (int x = 0; x < Config.CHUNK_SIZE.x; x++) {
            for (int y = 0; y < Config.CHUNK_SIZE.y; y++) {
                for (int z = 0; z < Config.CHUNK_SIZE.z; z++) {
                    chunk.setBlock(new Vector3i(x, y, z), new Block(blockType));
                }
            }
        }
    }

    private static Unsafe getUnsafe() throws Exception {
        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        return (Unsafe) unsafeField.get(null);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = ClientWorld.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
