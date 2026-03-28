package com.ee;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.joml.Vector2i;
import org.joml.Vector3i;
import org.junit.jupiter.api.Test;

import com.ee.Common.BlockType;
import com.ee.Common.CompressedChunk;
import com.ee.Common.Network.BlockUpdate;
import com.ee.Common.Network.ChunkRequest;
import com.ee.Common.Network.ChunkResponse;

public class NetworkPacketTest {

    @Test
    public void chunkRequestRoundTripsCoordinates() {
        ChunkRequest request = new ChunkRequest(-12, 34);

        ChunkRequest deserialized = ChunkRequest.deserialize(request.serialize(), 9);

        assertEquals(-12, deserialized.chunkX());
        assertEquals(34, deserialized.chunkZ());
    }

    @Test
    public void blockUpdateRoundTripsPositionAndType() {
        BlockUpdate update = new BlockUpdate(-4, 99, 123, BlockType.OakLeaves);

        BlockUpdate deserialized = BlockUpdate.deserialize(update.serialize(), 14);

        assertEquals(new Vector3i(-4, 99, 123), deserialized.blockPos());
        assertEquals(BlockType.OakLeaves, deserialized.blockType());
    }

    @Test
    public void chunkResponseRoundTripsCompressedChunkPayload() {
        byte[] compressedData = new byte[] { 9, 8, 7, 6 };
        ChunkResponse response = new ChunkResponse(new CompressedChunk(new Vector2i(-3, 5), 123456, compressedData));

        ChunkResponse deserialized = ChunkResponse.deserialize(response.serialize(), response.serialize().length);

        assertEquals(-3, deserialized.chunkX());
        assertEquals(5, deserialized.chunkZ());
        assertEquals(123456, deserialized.hash());
        assertArrayEquals(compressedData, deserialized.compressedData());
    }
}
