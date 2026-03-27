package com.ee.Server;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import org.joml.Vector2i;
import com.ee.Common.Network.*;
import com.ee.Common.Chunk;
import com.ee.Common.CompressedChunk;

public class Listener implements Runnable, AutoCloseable {
    private DatagramSocket socket;
    private ServerWorld world;

    public Listener(int port, ServerWorld world) {
        this.world = world;
        try {
            this.socket = new DatagramSocket(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            this.socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                this.socket.receive(packet);
                System.out.println("Received packet of length: " + packet.getLength() + ", source: "
                        + packet.getAddress() + ":" + packet.getPort() + ", content: "
                        + new String(packet.getData(), 0, packet.getLength()));
                handlePacket(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handlePacket(DatagramPacket packet) {
        byte[] data = packet.getData();
        if (data[0] == (byte) PacketType.CHUNK_REQUEST.ordinal()) {
            ChunkRequest request = ChunkRequest.deserialize(data, packet.getLength());
            System.out.println("Received chunk request for chunk (" + request.chunkX() + ", " + request.chunkZ() + ")");
            handleChunkRequest(request, packet);
        } else {
            System.out.println("Received unknown packet type: " + data[0]);
        }
    }

    private void handleChunkRequest(ChunkRequest request, DatagramPacket packet) {
        Chunk chunkData = world.getOrGenerateChunk(new Vector2i(request.chunkX(), request.chunkZ()));
        CompressedChunk compressedChunk = new CompressedChunk(chunkData);
        if (chunkData != null) {
            ChunkResponse response = new ChunkResponse(compressedChunk);
            byte[] responseData = response.serialize();
            DatagramPacket udpPacket = new DatagramPacket(responseData, responseData.length, packet.getAddress(),
                    packet.getPort());
            try {
                socket.send(udpPacket);
                System.out.println("Sent chunk response for chunk (" + request.chunkX() + ", " + request.chunkZ() + ")");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Chunk (" + request.chunkX() + ", " + request.chunkZ() + ") not found");
        }
    }
}
