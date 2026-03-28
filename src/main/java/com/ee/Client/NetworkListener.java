package com.ee.Client;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.util.concurrent.atomic.AtomicLong;
import com.ee.Common.Block;
import com.ee.Common.Chunk;
import com.ee.Common.CompressedChunk;
import com.ee.Common.Network.*;

public class NetworkListener implements Runnable {
    private DatagramSocket socket;
    private ClientWorld world;
    private AtomicLong receivedPacketCount;

    public NetworkListener(DatagramSocket socket, ClientWorld world) {
        this.socket = socket;
        this.world = world;
        this.receivedPacketCount = new AtomicLong();
    }

    @Override
    public void run() {
        while (!this.socket.isClosed()) {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                this.socket.receive(packet);
                handlePacket(packet);
            } catch (Exception e) {
                if (!this.socket.isClosed()) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handlePacket(DatagramPacket packet) {
        receivedPacketCount.incrementAndGet();
        byte[] data = packet.getData();
        if (data[0] == (byte) PacketType.CHUNK_RESPONSE.ordinal()) {
            processChunkResponse(packet, data);
        } else if (data[0] == (byte) PacketType.BLOCK_UPDATE.ordinal()) {
            processBlockUpdate(packet, data);
        } else {
            System.out.println("[NET] Received: " + data[0]);
        }
    }

    private void processBlockUpdate(DatagramPacket packet, byte[] data) {
        BlockUpdate update = BlockUpdate.deserialize(data, packet.getLength());
        System.out.println("[NET] Receive Block Update " + update.blockPos() + ", " + update.blockType());
        try {
            world.applyBlockUpdate(update.blockPos(), new Block(update.blockType()));
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Failed to apply block update: " + e.toString());
        }
    }

    private void processChunkResponse(DatagramPacket packet, byte[] data) {
        ChunkResponse response = ChunkResponse.deserialize(data,
                packet.getLength());
        System.out.println("[NET] Receive chunk update " + response.chunkX() + ", " + response.chunkZ());
        CompressedChunk compressedChunk = response.toCompressedChunk();
        Chunk chunk = compressedChunk.decompress();
        world.addChunk(chunk.worldPosition(), chunk);
    }

    public long receivedPacketCount() {
        return receivedPacketCount.get();
    }
}
