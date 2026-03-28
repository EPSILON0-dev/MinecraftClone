package com.ee;

import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.junit.jupiter.api.Test;

import com.ee.Client.ChunkRenderer;
import com.ee.Common.Block;
import com.ee.Common.BlockType;
import com.ee.Common.Chunk;
import com.ee.Common.Config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ChunkTest {

    @Test
    public void setBlockStoresAndReturnsBlockAtPosition() {
        Chunk chunk = new Chunk(new Vector2i(0, 0));
        Block block = new Block(BlockType.Dirt);
        Vector3i position = new Vector3i(1, 2, 3);

        chunk.setBlock(position, block);

        assertEquals(BlockType.Dirt, chunk.getBlock(position).type);
    }

    @Test
    public void setBlockRejectsOutOfBoundsPositions() {
        Chunk chunk = new Chunk(new Vector2i(0, 0));

        assertThrows(IndexOutOfBoundsException.class,
                () -> chunk.setBlock(new Vector3i(-1, 0, 0), new Block(BlockType.Dirt)));
        assertThrows(IndexOutOfBoundsException.class,
                () -> chunk.setBlock(new Vector3i(Config.CHUNK_SIZE.x, 0, 0), new Block(BlockType.Dirt)));
        assertThrows(IndexOutOfBoundsException.class,
                () -> chunk.setBlock(new Vector3i(0, Config.CHUNK_SIZE.y, 0), new Block(BlockType.Dirt)));
        assertThrows(IndexOutOfBoundsException.class,
                () -> chunk.setBlock(new Vector3i(0, 0, Config.CHUNK_SIZE.z), new Block(BlockType.Dirt)));
    }

    @Test
    public void generateBlocksAssignsExpectedBlockTypesByHeight() {
        Chunk chunk = new Chunk(new Vector2i(0, 0));

        chunk.generateBlocks();

        assertEquals(BlockType.Air, chunk.getBlock(new Vector3i(0, 65, 0)).type);
        assertEquals(BlockType.Grass, chunk.getBlock(new Vector3i(0, 64, 0)).type);
        assertEquals(BlockType.Dirt, chunk.getBlock(new Vector3i(0, 61, 0)).type);
        assertEquals(BlockType.Cobblestone, chunk.getBlock(new Vector3i(0, 60, 0)).type);
    }

    @Test
    public void generateHashIsStableForEquivalentChunks() {
        Chunk left = new Chunk(new Vector2i(1, 2));
        Chunk right = new Chunk(new Vector2i(1, 2));

        left.generateBlocks();
        right.generateBlocks();

        assertEquals(left.computeHash(), right.computeHash());
    }

    @Test
    public void generateHashChangesWhenChunkContentsChange() {
        Chunk chunk = new Chunk(new Vector2i(0, 0));
        chunk.generateBlocks();

        int originalHash = chunk.computeHash();
        chunk.setBlock(new Vector3i(0, 64, 0), new Block(BlockType.Stone));

        assertEquals(false, originalHash == chunk.computeHash());
    }

    @Test
    public void modelMatrixTranslatesChunkToWorldPosition() {
        Chunk chunk = new Chunk(new Vector2i(1, 2));
        ChunkRenderer chunkRenderer = new ChunkRenderer(chunk);

        Vector3f translation = chunkRenderer.modelMatrix().getTranslation(new Vector3f());

        assertEquals(16.0f, translation.x, 0.0001f);
        assertEquals(0.0f, translation.y, 0.0001f);
        assertEquals(32.0f, translation.z, 0.0001f);
    }
}
