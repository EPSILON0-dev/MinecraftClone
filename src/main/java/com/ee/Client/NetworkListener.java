package com.ee.Client;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import com.ee.Common.Chunk;
import com.ee.Common.CompressedChunk;
import com.ee.Common.Network.*;

public class NetworkListener implements Runnable {
    private DatagramSocket socket;
    private ClientWorld world;

    public NetworkListener(DatagramSocket socket, ClientWorld world) {
        this.socket = socket;
        this.world = world;
    }

    @Override
    public void run() {
        while (true) {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                this.socket.receive(packet);
                handlePacket(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handlePacket(DatagramPacket packet) {
        byte[] data = packet.getData();
        if (data[0] == (byte) PacketType.CHUNK_RESPONSE.ordinal()) {
            ChunkResponse response = ChunkResponse.deserialize(data,
                    packet.getLength());
            System.out.println("[NET] Receive " + response.chunkX() + ", " + response.chunkZ());
            CompressedChunk compressedChunk = response.toCompressedChunk();
            Chunk chunk = compressedChunk.decompress();
            world.addChunk(chunk.worldPosition(), chunk);
        } else {
            System.out.println("[NET] Received: " + data[0]);
        }
    }
}
