package com.ee;

import org.joml.Vector2i;
import org.joml.Vector3i;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WorldTest {

    @Test
    public void constructorRejectsNonPositiveChunkCounts() {
        assertThrows(IndexOutOfBoundsException.class, () -> new World(new Vector2i(0, 1), false));
        assertThrows(IndexOutOfBoundsException.class, () -> new World(new Vector2i(1, 0), false));
        assertThrows(IndexOutOfBoundsException.class, () -> new World(new Vector2i(-1, 1), false));
    }

    @Test
    public void setBlockStoresAndReturnsBlockAtWorldPosition() {
        World world = new World(new Vector2i(1, 1), false);
        Vector3i worldPosition = new Vector3i(3, 10, 4);

        world.setBlock(worldPosition, new Block(BlockType.Dirt));

        assertEquals(BlockType.Dirt, world.getBlock(worldPosition).type);
    }

    @Test
    public void setBlockMapsExactNegativeChunkBoundariesToCorrectChunk() {
        World world = new World(new Vector2i(2, 1), false);
        Vector3i worldPosition = new Vector3i(-Config.CHUNK_SIZE.x, 10, 0);

        world.setBlock(worldPosition, new Block(BlockType.Grass));

        assertEquals(BlockType.Grass, world.getBlock(worldPosition).type);
    }

    @Test
    public void getBlockThrowsWhenWorldPositionIsOutsideLoadedChunks() {
        World world = new World(new Vector2i(1, 1), false);

        assertThrows(IndexOutOfBoundsException.class,
                () -> world.getBlock(new Vector3i(Config.CHUNK_SIZE.x, 0, 0)));
        assertThrows(IndexOutOfBoundsException.class,
                () -> world.getBlock(new Vector3i(0, Config.CHUNK_SIZE.y, 0)));
    }

    @Test
    public void getBlockNoThrowReturnsAirForInvalidPositionsAndStoredBlockForValidOnes() {
        World world = new World(new Vector2i(1, 1), false);
        Vector3i validPosition = new Vector3i(2, 12, 2);

        world.setBlock(validPosition, new Block(BlockType.Cobblestone));

        assertEquals(BlockType.Cobblestone, world.getBlockNoThrow(validPosition).type);
        assertEquals(BlockType.Air, world.getBlockNoThrow(new Vector3i(0, -1, 0)).type);
        assertEquals(BlockType.Air, world.getBlockNoThrow(new Vector3i(0, Config.CHUNK_SIZE.y, 0)).type);
        assertEquals(BlockType.Air, world.getBlockNoThrow(new Vector3i(Config.CHUNK_SIZE.x, 0, 0)).type);
    }
}
