package com.ee.Common;

import org.joml.*;

public class Chunk {
    protected Vector2i worldPosition;
    protected Block[] blocks;

    public Chunk(Vector2i worldPosition) {
        this.worldPosition = worldPosition;
        this.blocks = new Block[Config.CHUNK_BLOCK_COUNT];
    }

    public Chunk(Vector2i worldPosition, Block[] blocks) throws IllegalArgumentException {
        if (blocks.length != Config.CHUNK_BLOCK_COUNT) {
            throw new IllegalArgumentException("Invalid block array length");
        }
        this.worldPosition = worldPosition;
        this.blocks = blocks;
    }

    public void generateBlocks() {
        try {
            for (int x = 0; x < Config.CHUNK_SIZE.x; x++) {
                for (int y = 0; y < Config.CHUNK_SIZE.y; y++) {
                    for (int z = 0; z < Config.CHUNK_SIZE.z; z++) {
                        generateBlock(new Vector3i(x, y, z));
                    }
                }
            }
        } catch (Exception e) {
            // Pass
        }
    }

    public void setBlock(Vector3i position, Block block) throws IndexOutOfBoundsException {
        blocks[positionToIndex(position)] = block;
    }

    public Block getBlock(Vector3i position) throws IndexOutOfBoundsException {
        return blocks[positionToIndex(position)];
    }

    public Vector2i worldPosition() {
        return worldPosition;
    }

    public Block[] getBlockArrayClone() {
        return blocks.clone();
    }

    public int computeHash() {
        int hash = 17;
        hash = 31 * hash + worldPosition.x;
        hash = 31 * hash + worldPosition.y;

        for (Block block : blocks) {
            int blockHash = (block == null) ? -1 : block.type.ordinal();
            hash = 31 * hash + blockHash;
        }

        return hash;
    }

    private void generateBlock(Vector3i position) {
        var block = (position.y > 64) ? new Block(BlockType.Air)
                : (position.y == 64) ? new Block(BlockType.Grass)
                : (position.y == 0) ? new Block(BlockType.Bedrock)
                        : (position.y > 60) ? new Block(BlockType.Dirt) : new Block(BlockType.Cobblestone);
        setBlock(position, block);
    }

    private int positionToIndex(Vector3i position) throws IndexOutOfBoundsException {
        if (position.x >= Config.CHUNK_SIZE.x || position.x < 0 || position.y >= Config.CHUNK_SIZE.y || position.y < 0
                || position.z >= Config.CHUNK_SIZE.z || position.z < 0) {
            throw new IndexOutOfBoundsException("Position out of bounds");
        }

        return position.x + position.z * Config.CHUNK_SIZE.x + position.y * Config.CHUNK_SIZE.x * Config.CHUNK_SIZE.z;
    }
}
