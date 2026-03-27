package com.ee.Common.Network;

public class ChunkRequest implements NetworkPacket {
    private final int chunkX;
    private final int chunkZ;

    public ChunkRequest(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public int chunkX() {
        return this.chunkX;
    }

    public int chunkZ() {
        return this.chunkZ;
    }

    @Override
    public byte[] serialize() {
        byte[] data = new byte[9];
        data[0] = (byte) PacketType.CHUNK_REQUEST.ordinal();
        data[1] = (byte) (this.chunkX >> 24);
        data[2] = (byte) (this.chunkX >> 16);
        data[3] = (byte) (this.chunkX >> 8);
        data[4] = (byte) this.chunkX;
        data[5] = (byte) (this.chunkZ >> 24);
        data[6] = (byte) (this.chunkZ >> 16);
        data[7] = (byte) (this.chunkZ >> 8);
        data[8] = (byte) this.chunkZ;
        return data;
    }

    public static ChunkRequest deserialize(byte[] data, int length) {
        if (data.length < length || length != 9) {
            throw new IllegalArgumentException("Data length is not valid for ChunkRequest");
        }
        PacketType type = PacketType.values()[data[0]];
        if (type != PacketType.CHUNK_REQUEST) {
            throw new IllegalArgumentException("Invalid packet type for ChunkRequest");
        }
        int chunkX = ((data[1] & 0xFF) << 24) | ((data[2] & 0xFF) << 16) | ((data[3] & 0xFF) << 8)
                | (data[4] & 0xFF);
        int chunkZ = ((data[5] & 0xFF) << 24) | ((data[6] & 0xFF) << 16) | ((data[7] & 0xFF) << 8)
                | (data[8] & 0xFF);
        return new ChunkRequest(chunkX, chunkZ);
    }
}
