package com.ee.Server;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.joml.Vector2i;
import com.ee.Common.Network.*;
import com.ee.Common.Block;
import com.ee.Common.Chunk;
import com.ee.Common.CompressedChunk;
import com.ee.Common.Config;

public class Listener implements Runnable, AutoCloseable {
    private DatagramSocket socket;
    private ServerWorld world;
    private HashMap<SocketAddress, Long> activeClients;
    private AtomicLong receivedPacketCount;
    private AtomicLong sentPacketCount;

    public Listener(int port, ServerWorld world) {
        this.world = world;
        this.activeClients = new HashMap<>();
        this.receivedPacketCount = new AtomicLong();
        this.sentPacketCount = new AtomicLong();
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
        registerClient(packet);
        receivedPacketCount.incrementAndGet();
        byte[] data = packet.getData();
        if (data[0] == (byte) PacketType.CHUNK_REQUEST.ordinal()) {
            handleChunkRequest(packet);
        } else if (data[0] == (byte) PacketType.BLOCK_UPDATE.ordinal()) {
            handleBlockUpdate(packet);
        } else if (data[0] == (byte) PacketType.HEARTBEAT.ordinal()) {
            handleHeartbeat(packet);
        } else {
            System.out.println("Received unknown packet type: " + data[0]);
        }
    }

    private void handleHeartbeat(DatagramPacket packet) {
        Heartbeat.deserialize(packet.getData(), packet.getLength());
    }

    private void handleChunkRequest(DatagramPacket packet) {
        ChunkRequest request = ChunkRequest.deserialize(packet.getData(), packet.getLength());
        System.out.println("[NET] Chunk Request " + request.chunkX() + ", " + request.chunkZ());
        Chunk chunkData = world.getOrGenerateChunk(new Vector2i(request.chunkX(), request.chunkZ()));
        CompressedChunk compressedChunk = new CompressedChunk(chunkData);
        if (chunkData != null) {
            respond(packet, new ChunkResponse(compressedChunk).serialize());
        }
    }

    private void handleBlockUpdate(DatagramPacket packet) {
        BlockUpdate request = BlockUpdate.deserialize(packet.getData(), packet.getLength());
        System.out
                .println("[NET] Block Update " + request.blockX() + ", " + request.blockY() + ", " + request.blockZ());
        world.setBlock(request.blockPos(), new Block(request.blockType()));
        broadcast(request.serialize());
    }

    private void respond(DatagramPacket packet, byte[] data) {
        DatagramPacket udpPacket = new DatagramPacket(data, data.length, packet.getAddress(), packet.getPort());
        try {
            socket.send(udpPacket);
            sentPacketCount.incrementAndGet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerClient(DatagramPacket packet) {
        pruneExpiredClients();
        SocketAddress clientAddress = new InetSocketAddress(packet.getAddress(), packet.getPort());
        activeClients.put(clientAddress, System.currentTimeMillis());
    }

    private void pruneExpiredClients() {
        long cutoff = System.currentTimeMillis() - Config.NETWORK_CLIENT_TTL_MS;
        Iterator<Map.Entry<SocketAddress, Long>> iterator = activeClients.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<SocketAddress, Long> entry = iterator.next();
            if (entry.getValue() < cutoff) {
                iterator.remove();
            }
        }
    }

    private void broadcast(byte[] data) {
        pruneExpiredClients();
        for (SocketAddress clientAddress : activeClients.keySet()) {
            DatagramPacket udpPacket = new DatagramPacket(data, data.length);
            udpPacket.setSocketAddress(clientAddress);
            try {
                socket.send(udpPacket);
                sentPacketCount.incrementAndGet();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public long receivedPacketCount() {
        return receivedPacketCount.get();
    }

    public long sentPacketCount() {
        return sentPacketCount.get();
    }
}
