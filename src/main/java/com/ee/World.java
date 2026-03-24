package com.ee;

import java.util.HashMap;
import java.lang.Math;
import org.joml.*;

public class World {
    private HashMap<Vector2i, Chunk> chunks;
    private Vector3i minPos, maxPos;

    public World(Vector2i chunkCount) throws IndexOutOfBoundsException {
        this(chunkCount, true);
    }

    World(Vector2i chunkCount, boolean generateMeshes) throws IndexOutOfBoundsException {
        // Sainitize the size
        if (chunkCount.x <= 0 || chunkCount.y <= 0) {
            throw new IndexOutOfBoundsException("World size must be greater than 0");
        }

        // Compute the limits
        var mid = new Vector2i(chunkCount).div(2);
        minPos = new Vector3i((mid.x - chunkCount.x) * Config.CHUNK_SIZE.x, 0,
                (mid.y - chunkCount.y) * Config.CHUNK_SIZE.z);
        maxPos = new Vector3i((chunkCount.x - mid.x) * Config.CHUNK_SIZE.x, Config.CHUNK_SIZE.y,
                (chunkCount.y - mid.y) * Config.CHUNK_SIZE.z);

        // Fill the chunks
        chunks = new HashMap<>();
        for (int y = 0; y < chunkCount.y; y++) {
            for (int x = 0; x < chunkCount.x; x++) {
                var pos = new Vector2i(x, y).sub(mid);
                var chunk = new Chunk(pos);
                chunk.generateBlocks();
                if (generateMeshes) {
                    chunk.generateMesh();
                }
                chunks.put(pos, chunk);
            }
        }
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
        if (!isPositionValid(worldPos)) {
            return new Block(BlockType.Air);
        }

        try {
            return getBlock(worldPos);
        } catch (Exception e) {
            return new Block(BlockType.Air);
        }
    }

    public boolean isSolid(int x, int y, int z) {
        return getBlockNoThrow(new Vector3i(x, y, z)).type != BlockType.Air;
    }

    public void generateChunkMesh(Vector3i worldPos) throws IndexOutOfBoundsException {
        var chunkPos = getChunkInWorld(worldPos);
        if (!chunks.containsKey(chunkPos)) {
            throw new IndexOutOfBoundsException("Chunk not found");
        }
        chunks.get(chunkPos).generateMesh();
    }

    public void renderChunks(Shader shader, String modelMatrixUniform) {
        for (Chunk chunk : chunks.values()) {
            shader.setUniformMatrix4f(modelMatrixUniform, chunk.modelMatrix());
            chunk.mesh().bind();
            chunk.mesh().draw();
            chunk.mesh().unbind();
        }
    }

    private boolean isPositionValid(Vector3i position) {
        return (position.x >= minPos.x) && (position.y >= minPos.y) && (position.z >= minPos.z) &&
                (position.x < maxPos.x) && (position.y < maxPos.y) && (position.z < maxPos.z);
    }

    private Vector3i getBlockInChunk(Vector3i position) {
        int x = (position.x >= 0) ? position.x % Config.CHUNK_SIZE.x
                : (Config.CHUNK_SIZE.x - 1 - (-position.x - 1) % Config.CHUNK_SIZE.x);
        int z = (position.z >= 0) ? position.z % Config.CHUNK_SIZE.z
                : (Config.CHUNK_SIZE.z - 1 - (-position.z - 1) % Config.CHUNK_SIZE.z);
        return new Vector3i(x, position.y, z);
    }

    private Vector2i getChunkInWorld(Vector3i position) {
        int x = Math.floorDiv(position.x, Config.CHUNK_SIZE.x);
        int z = Math.floorDiv(position.z, Config.CHUNK_SIZE.z);
        return new Vector2i(x, z);
    }
}
