package com.ee.Common;

import java.util.zip.Deflater;
import org.joml.Vector2i;
import java.util.Arrays;

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
        compressedData = Arrays.copyOf(buffer, len);
        deflater.deflate(compressedData);
        deflater.end();

        worldPosition = chunk.worldPosition;
        hash = chunk.computeHash();
    }

    public int getCompressedSize() {
        return compressedData.length;
    }

    public Chunk decompress() throws IllegalStateException {
        byte[] decompressedData = new byte[Config.CHUNK_BLOCK_COUNT * Config.BLOCK_DATA_SIZE];
        java.util.zip.Inflater inflater = new java.util.zip.Inflater();
        inflater.setInput(compressedData);
        try {
            inflater.inflate(decompressedData);
        } catch (java.util.zip.DataFormatException e) {
            e.printStackTrace();
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
