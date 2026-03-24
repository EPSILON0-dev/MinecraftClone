package com.ee;

import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.junit.jupiter.api.Test;
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
        World world = createWorld(new Vector3i(0, 64, 0), BlockType.Grass);
        Camera camera = new Camera(new Vector3f(0.5f, 65.5f, 0.5f), new Vector3f(0.0f, -1.0f, 0.0f),
                90.0f, 1.0f);

        Optional<Vector3i> hit = RayCast.rayCast(camera, world, 5.0f, false);

        assertTrue(hit.isPresent());
        assertEquals(new Vector3i(0, 64, 0), hit.get());
    }

    @Test
    public void rayCastReturnsPreviousAirBlockBeforeFirstSolidBlockWhenRequested() throws Exception {
        World world = createWorld(new Vector3i(0, 64, 0), BlockType.Grass);
        Camera camera = new Camera(new Vector3f(0.5f, 65.5f, 0.5f), new Vector3f(0.0f, -1.0f, 0.0f),
                90.0f, 1.0f);

        Optional<Vector3i> hit = RayCast.rayCast(camera, world, 5.0f, true);

        assertTrue(hit.isPresent());
        assertEquals(new Vector3i(0, 65, 0), hit.get());
    }

    @Test
    public void rayCastReturnsCurrentBlockWhenCameraStartsInsideSolidBlock() throws Exception {
        World world = createWorld(new Vector3i(2, 10, 3), BlockType.Cobblestone);
        Camera camera = new Camera(new Vector3f(2.5f, 10.5f, 3.5f), new Vector3f(1.0f, 0.0f, 0.0f),
                90.0f, 1.0f);

        Optional<Vector3i> hit = RayCast.rayCast(camera, world, 5.0f, false);

        assertTrue(hit.isPresent());
        assertEquals(new Vector3i(2, 10, 3), hit.get());
    }

    @Test
    public void rayCastReturnsCurrentBlockWhenPreviousRequestedAndCameraStartsInsideSolidBlock() throws Exception {
        World world = createWorld(new Vector3i(2, 10, 3), BlockType.Cobblestone);
        Camera camera = new Camera(new Vector3f(2.5f, 10.5f, 3.5f), new Vector3f(1.0f, 0.0f, 0.0f),
                90.0f, 1.0f);

        Optional<Vector3i> hit = RayCast.rayCast(camera, world, 5.0f, true);

        assertTrue(hit.isPresent());
        assertEquals(new Vector3i(2, 10, 3), hit.get());
    }

    @Test
    public void rayCastReturnsEmptyWhenNoSolidBlockIsHit() throws Exception {
        World world = createWorld(null, null);
        Camera camera = new Camera(new Vector3f(0.5f, 65.5f, 0.5f), new Vector3f(0.0f, 1.0f, 0.0f),
                90.0f, 1.0f);

        Optional<Vector3i> hit = RayCast.rayCast(camera, world, 5.0f, false);

        assertFalse(hit.isPresent());
    }

    @Test
    public void rayCastReturnsEmptyWhenPreviousRequestedAndNoSolidBlockIsHit() throws Exception {
        World world = createWorld(null, null);
        Camera camera = new Camera(new Vector3f(0.5f, 65.5f, 0.5f), new Vector3f(0.0f, 1.0f, 0.0f),
                90.0f, 1.0f);

        Optional<Vector3i> hit = RayCast.rayCast(camera, world, 5.0f, true);

        assertFalse(hit.isPresent());
    }

    private static World createWorld(Vector3i solidBlockPosition, BlockType solidBlockType) throws Exception {
        World world = (World) getUnsafe().allocateInstance(World.class);
        Chunk chunk = new Chunk(new Vector2i(0, 0));
        fillChunk(chunk, BlockType.Air);
        if (solidBlockPosition != null && solidBlockType != null) {
            chunk.setBlock(solidBlockPosition, new Block(solidBlockType));
        }

        HashMap<Vector2i, Chunk> chunks = new HashMap<>();
        chunks.put(new Vector2i(0, 0), chunk);

        setField(world, "chunks", chunks);
        setField(world, "minPos", new Vector3i(0, 0, 0));
        setField(world, "maxPos", new Vector3i(Config.CHUNK_SIZE.x, Config.CHUNK_SIZE.y, Config.CHUNK_SIZE.z));
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
        Field field = World.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
