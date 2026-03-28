package com.ee.Common;

import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import org.joml.Vector2i;

public class CompressedChunk {
    private byte[] compressedData;
    private Vector2i worldPosition;
    private int hash;

    public CompressedChunk(Chunk chunk) {
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, true);
        Block[] data = chunk.getBlockArrayClone();

        // WARN: Very naive and WILL break when the block size is changed
        byte[] bytes = new byte[data.length * Config.BLOCK_DATA_SIZE];
        for (int i = 0; i < data.length; i++) {
            bytes[i] = (byte) ((data[i] == null) ? -1 : data[i].type.ordinal());
        }

        deflater.setInput(bytes);
        deflater.finish();

        byte[] buffer = new byte[bytes.length];
        int len = deflater.deflate(buffer);
        compressedData = new byte[len];
        System.arraycopy(buffer, 0, compressedData, 0, len);
        deflater.end();

        worldPosition = chunk.worldPosition;
        hash = chunk.computeHash();
    }

    public CompressedChunk(Vector2i worldPosition, int hash, byte[] compressedData) {
        this.worldPosition = worldPosition;
        this.hash = hash;
        this.compressedData = compressedData;
    }

    public Vector2i worldPosition() {
        return worldPosition;
    }

    public int hash() {
        return hash;
    }

    public byte[] compressedData() {
        return compressedData;
    }

    public int getCompressedSize() {
        return compressedData.length;
    }

    public Chunk decompress() throws IllegalStateException {
        byte[] decompressedData = new byte[Config.CHUNK_BLOCK_COUNT * Config.BLOCK_DATA_SIZE];
        Inflater inflater = new Inflater(true);
        inflater.setInput(compressedData);
        try {
            inflater.inflate(decompressedData);
        } catch (DataFormatException e) {
            throw new IllegalStateException("Failed to decompress chunk data", e);
        } finally {
            inflater.end();
        }

        Block[] blocks = new Block[Config.CHUNK_BLOCK_COUNT];
        for (int i = 0; i < blocks.length; i++) {
            int blockTypeOrdinal = decompressedData[i];
            if (blockTypeOrdinal != -1) {
                blocks[i] = new Block(BlockType.values()[blockTypeOrdinal]);
            }
        }

        var chunk = new Chunk(new Vector2i(worldPosition), blocks);
        if (chunk.computeHash() != hash) {
            throw new IllegalStateException("Decompressed chunk hash does not match original");
        }

        return chunk;
    }
}
