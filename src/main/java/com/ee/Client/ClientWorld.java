package com.ee.Client;

import java.util.HashMap;
import java.util.Optional;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.lang.Math;
import org.joml.*;

import com.ee.Common.Block;
import com.ee.Common.BlockType;
import com.ee.Common.Chunk;
import com.ee.Common.Config;
import com.ee.Common.Util;

public class ClientWorld {
    private Semaphore worldLock = new Semaphore(1);
    private HashMap<Vector2i, ChunkRenderer> chunks;
    private LinkedList<ChunkRenderer> chunksToGenerateMesh;
    // TODO Give requests TTL
    private LinkedList<Vector2i> requestedChunks;

    public ClientWorld() {
        chunks = new HashMap<>();
        chunksToGenerateMesh = new LinkedList<>();
        requestedChunks = new LinkedList<>();
    }

    public void addChunk(Vector2i chunkPos, Chunk chunk) {
        try {
            var renderer = new ChunkRenderer(chunk);
            worldLock.acquire();
            if (requestedChunks.contains(chunkPos)) {
                requestedChunks.remove(chunkPos);
            }
            chunks.put(chunkPos, renderer);
            chunksToGenerateMesh.add(renderer);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            worldLock.release();
        }
    }

    public void setBlock(Vector3i worldPos, Block block) throws IndexOutOfBoundsException {
        try {
            worldLock.acquire();
            var chunkPos = getChunkInWorld(worldPos);
            if (!chunks.containsKey(chunkPos)) {
                throw new IndexOutOfBoundsException("Chunk not found");
            }
            var blockPos = getBlockInChunk(worldPos);
            chunks.get(chunkPos).setBlock(blockPos, block);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            worldLock.release();
        }
    }

    public Block getBlock(Vector3i worldPos) throws IndexOutOfBoundsException {
        try {
            worldLock.acquire();
            var chunkPos = getChunkInWorld(worldPos);
            if (!chunks.containsKey(chunkPos)) {
                throw new IndexOutOfBoundsException("Chunk not found");
            }
            var blockPos = getBlockInChunk(worldPos);
            return chunks.get(chunkPos).getBlock(blockPos);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return new Block(BlockType.Air);
        } finally {
            worldLock.release();
        }
    }

    public Block getBlockNoThrow(Vector3i worldPos) {
        try {
            return getBlock(worldPos);
        } catch (Exception e) {
            return new Block(BlockType.Air);
        }
    }

    public Chunk getChunk(Vector2i chunkPos) {
        try {
            worldLock.acquire();
            return chunks.getOrDefault(chunkPos, null);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } finally {
            worldLock.release();
        }
    }

    public boolean containsChunk(Vector2i chunkPos) {
        return chunks.containsKey(chunkPos);
    }

    public boolean isSolid(int x, int y, int z) {
        return getBlockNoThrow(new Vector3i(x, y, z)).type != BlockType.Air;
    }

    public void requestMeshGeneration(Vector2i chunkPos) throws IndexOutOfBoundsException {
        try {
            worldLock.acquire();
            if (!chunks.containsKey(chunkPos)) {
                throw new IndexOutOfBoundsException("Chunk not found");
            }
            var chunk = chunks.get(chunkPos);
            if (!chunksToGenerateMesh.contains(chunk))
                chunksToGenerateMesh.add(chunk);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            worldLock.release();
        }
    }

    public void generateQueuedMeshes() {
        // TODO move mesh generation onto another thread
        try {
            worldLock.acquire();
            for (ChunkRenderer chunk : chunksToGenerateMesh) {
                chunk.generateMeshData();
                chunk.generateMesh();
            }
            chunksToGenerateMesh.clear();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            worldLock.release();
        }
    }

    public void renderChunks(Shader shader, String modelMatrixUniform) {
        ChunkRenderer renderers[] = null;
        try {
            worldLock.acquire();
            renderers = new ChunkRenderer[chunks.values().size()];
            chunks.values().toArray(renderers);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            worldLock.release();
        }
        if (renderers == null)
            return;

        for (ChunkRenderer chunk : renderers) {
            if (!chunk.isReadyToRender())
                continue;
            shader.setUniformMatrix4f(modelMatrixUniform, chunk.modelMatrix());
            chunk.mesh().bind();
            chunk.mesh().draw();
            chunk.mesh().unbind();
        }
    }

    public Optional<Vector2i> getNearestMissingChunk(Player player) {
        // TODO find a better way to do that
        Vector2i bestCandidate = new Vector2i(0, 0);
        float bestDistance = Float.MAX_VALUE;
        boolean found = false;
        var playerChunk = getChunkInWorld(Util.vec3fToVec3i(player.position()));
        int renderDistance = Config.WORLD_CHUNK_DISTANCE;
        for (int y = playerChunk.y - renderDistance; y < playerChunk.y + renderDistance; y++) {
            for (int x = playerChunk.x - renderDistance; x < playerChunk.x + renderDistance; x++) {
                var chunk = new Vector2i(x, y);
                if (!containsChunk(chunk) && chunk.distance(playerChunk) < bestDistance
                        && !requestedChunks.contains(chunk)) {
                    bestCandidate = chunk;
                    bestDistance = (float) chunk.distance(playerChunk);
                    found = true;
                }
            }
        }
        return (found) ? Optional.of(bestCandidate) : Optional.empty();
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
}
