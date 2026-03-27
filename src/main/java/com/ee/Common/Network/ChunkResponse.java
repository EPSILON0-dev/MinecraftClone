package com.ee.Common.Network;

import com.ee.Common.CompressedChunk;
import org.joml.*;

public class ChunkResponse implements NetworkPacket {
    private final int chunkX;
    private final int chunkZ;
    private final int hash;
    private final byte[] compressedData;

    public ChunkResponse(CompressedChunk compressedChunk) {
        this.chunkX = compressedChunk.worldPosition().x();
        this.chunkZ = compressedChunk.worldPosition().y();
        this.hash = compressedChunk.hash();
        this.compressedData = compressedChunk.compressedData();
    }

    public ChunkResponse(int chunkX, int chunkZ, int hash, byte[] compressedData) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.hash = hash;
        this.compressedData = compressedData;
    }

    public int chunkX() {
        return this.chunkX;
    }

    public int chunkZ() {
        return this.chunkZ;
    }

    public int hash() {
        return this.hash;
    }

    public byte[] compressedData() {
        return this.compressedData;
    }

    public CompressedChunk toCompressedChunk() {
        return new CompressedChunk(new Vector2i(this.chunkX, this.chunkZ), this.hash, this.compressedData);
    }

    @Override
    public byte[] serialize() {
        byte[] data = new byte[13 + compressedData.length];
        data[0] = (byte) PacketType.CHUNK_RESPONSE.ordinal();
        data[1] = (byte) (chunkX >> 24);
        data[2] = (byte) (chunkX >> 16);
        data[3] = (byte) (chunkX >> 8);
        data[4] = (byte) chunkX;
        data[5] = (byte) (chunkZ >> 24);
        data[6] = (byte) (chunkZ >> 16);
        data[7] = (byte) (chunkZ >> 8);
        data[8] = (byte) chunkZ;
        data[9] = (byte) (hash >> 24);
        data[10] = (byte) (hash >> 16);
        data[11] = (byte) (hash >> 8);
        data[12] = (byte) hash;
        System.arraycopy(compressedData, 0, data, 13, compressedData.length);
        return data;
    }

    public static ChunkResponse deserialize(byte[] data, int length) {
        int chunkX = (data[1] << 24) | ((data[2] & 0xFF) << 16) | ((data[3] & 0xFF) << 8) | (data[4] & 0xFF);
        int chunkZ = (data[5] << 24) | ((data[6] & 0xFF) << 16) | ((data[7] & 0xFF) << 8) | (data[8] & 0xFF);
        int hash = (data[9] << 24) | ((data[10] & 0xFF) << 16) | ((data[11] & 0xFF) << 8) | (data[12] & 0xFF);
        byte[] compressedData = new byte[length - 13];
        System.arraycopy(data, 13, compressedData, 0, compressedData.length);
        return new ChunkResponse(new CompressedChunk(new Vector2i(chunkX, chunkZ), hash, compressedData));
    }
}
