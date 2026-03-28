package com.ee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicLong;

import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.junit.jupiter.api.Test;

import com.ee.Client.ClientWorld;
import com.ee.Client.Player;
import com.ee.Common.Chunk;
import com.ee.Common.Config;

public class ClientWorldTest {

    @Test
    public void getBlockInChunkMapsNegativeCoordinatesIntoChunkSpace() {
        assertEquals(new Vector3i(15, 7, 15), ClientWorld.getBlockInChunk(new Vector3i(-1, 7, -1)));
        assertEquals(new Vector3i(0, 7, 0), ClientWorld.getBlockInChunk(new Vector3i(16, 7, 16)));
    }

    @Test
    public void getChunkInWorldUsesFloorDivisionForNegativeCoordinates() {
        assertEquals(new Vector2i(-1, -1), ClientWorld.getChunkInWorld(new Vector3i(-1, 10, -1)));
        assertEquals(new Vector2i(-1, 0), ClientWorld.getChunkInWorld(new Vector3i(-16, 10, 15)));
    }

    @Test
    public void missingChunkRequestExpiresAfterTtl() {
        AtomicLong now = new AtomicLong(1_000L);
        ClientWorld world = new ClientWorld(now::get);
        Player player = new Player(new Vector3f(0.5f, 64.0f, 0.5f), new Vector3f(1.0f, 0.0f, 0.0f));
        Vector2i expectedMissingChunk = new Vector2i(0, 0);
        int renderDistance = Config.WORLD_CHUNK_DISTANCE;

        for (int z = -renderDistance; z < renderDistance; z++) {
            for (int x = -renderDistance; x < renderDistance; x++) {
                Vector2i chunkPos = new Vector2i(x, z);
                if (!chunkPos.equals(expectedMissingChunk)) {
                    world.addChunk(chunkPos, new Chunk(new Vector2i(chunkPos)));
                }
            }
        }

        var firstRequest = world.getNearestMissingChunk(player);
        var secondRequest = world.getNearestMissingChunk(player);

        assertTrue(firstRequest.isPresent());
        assertEquals(expectedMissingChunk, firstRequest.get());
        assertFalse(secondRequest.isPresent());

        now.addAndGet(Config.NETWORK_CHUNK_REQUEST_TTL_MS + 1L);

        var thirdRequest = world.getNearestMissingChunk(player);

        assertTrue(thirdRequest.isPresent());
        assertEquals(expectedMissingChunk, thirdRequest.get());
    }

    @Test
    public void distantChunksUnloadAfterConfiguredTtl() {
        AtomicLong now = new AtomicLong(5_000L);
        ClientWorld world = new ClientWorld(now::get, 1);
        Player player = new Player(new Vector3f(0.5f, 64.0f, 0.5f), new Vector3f(1.0f, 0.0f, 0.0f));
        Vector2i distantChunk = new Vector2i(2, 0);

        world.addChunk(distantChunk, new Chunk(new Vector2i(distantChunk)));

        world.unloadDistantChunks(player);

        assertTrue(world.containsChunk(distantChunk));
        assertEquals(1, world.loadedChunkCount());

        now.addAndGet(Config.WORLD_CHUNK_UNLOAD_TTL_MS + 1L);
        world.unloadDistantChunks(player);

        assertFalse(world.containsChunk(distantChunk));
        assertEquals(0, world.loadedChunkCount());
    }
}