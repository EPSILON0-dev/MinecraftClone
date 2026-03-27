package com.ee.Server;

import java.util.HashMap;
import java.lang.Math;
import org.joml.*;

import com.ee.Common.Block;
import com.ee.Common.BlockType;
import com.ee.Common.Chunk;
import com.ee.Common.Config;

public class ServerWorld implements AutoCloseable {
    private HashMap<Vector2i, Chunk> chunks;

    public ServerWorld() {
        chunks = new HashMap<>();
    }

    @Override
    public void close() {
    }

    public Chunk getOrGenerateChunk(Vector2i chunkPos) {
        if (!chunks.containsKey(chunkPos)) {
            var chunk = new Chunk(chunkPos);
            chunk.generateBlocks();
            chunks.put(chunkPos, chunk);
        }
        return chunks.get(chunkPos);
    }

    public void setBlock(Vector3i worldPos, Block block) throws IndexOutOfBoundsException {
        var chunkPos = getChunkInWorld(worldPos);
        if (!chunks.containsKey(chunkPos)) {
            throw new IndexOutOfBoundsException("Chunk not found");
        }
        var blockPos = getBlockInChunk(worldPos);
        chunks.get(chunkPos).setBlock(blockPos, block);
    }

    public Block getBlock(Vector3i worldPos) throws IndexOutOfBoundsException {
        var chunkPos = getChunkInWorld(worldPos);
        if (!chunks.containsKey(chunkPos)) {
            throw new IndexOutOfBoundsException("Chunk not found");
        }
        var blockPos = getBlockInChunk(worldPos);
        return chunks.get(chunkPos).getBlock(blockPos);
    }

    public Block getBlockNoThrow(Vector3i worldPos) {
        try {
            return getBlock(worldPos);
        } catch (Exception e) {
            return new Block(BlockType.Air);
        }
    }

    public Chunk getChunk(Vector2i chunkPos) {
        return chunks.getOrDefault(chunkPos, null);
    }

    public Chunk getChunkAtPos(Vector3i chunkPos) {
        return chunks.getOrDefault(getChunkInWorld(chunkPos), null);
    }

    public static Vector3i getBlockInChunk(Vector3i position) {
        int x = (position.x >= 0) ? position.x % Config.CHUNK_SIZE.x
                : (Config.CHUNK_SIZE.x - 1 - (-position.x - 1) % Config.CHUNK_SIZE.x);
        int z = (position.z >= 0) ? position.z % Config.CHUNK_SIZE.z
                : (Config.CHUNK_SIZE.z - 1 - (-position.z - 1) % Config.CHUNK_SIZE.z);
        return new Vector3i(x, position.y, z);
    }

    public static Vector2i getChunkInWorld(Vector3i position) {
        int x = Math.floorDiv(position.x, Config.CHUNK_SIZE.x);
        int z = Math.floorDiv(position.z, Config.CHUNK_SIZE.z);
        return new Vector2i(x, z);
    }

    public boolean hasChunk(Vector2i chunkPos) {
        return chunks.containsKey(chunkPos);
    }

    public boolean isSolid(int x, int y, int z) {
        return getBlockNoThrow(new Vector3i(x, y, z)).type != BlockType.Air;
    }
}
