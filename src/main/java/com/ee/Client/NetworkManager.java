package com.ee.Client;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.joml.Vector3i;

import com.ee.Common.BlockType;
import com.ee.Common.Config;
import com.ee.Common.Network.*;

public class NetworkManager implements AutoCloseable {
    private DatagramSocket socket;
    private NetworkListener listener;
    private ClientWorld world;
    private ScheduledExecutorService heartbeatScheduler;
    private String serverHost;
    private int serverPort;
    private AtomicLong sentPacketCount;

    public NetworkManager(ClientWorld world) {
        this(world, Config.NETWORK_SERVER_HOST, Config.NETWORK_SERVER_PORT);
    }

    public NetworkManager(ClientWorld world, String serverHost, int serverPort) {
        this.world = world;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.sentPacketCount = new AtomicLong();
        try {
            this.socket = new DatagramSocket();
            this.listener = new NetworkListener(this.socket, this.world);
            new Thread(this.listener).start();
            this.heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable, "network-heartbeat");
                thread.setDaemon(true);
                return thread;
            });
            this.heartbeatScheduler.scheduleAtFixedRate(
                    this::sendHeartbeat,
                    0,
                    Config.NETWORK_HEARTBEAT_INTERVAL_MS,
                    TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            if (this.heartbeatScheduler != null) {
                this.heartbeatScheduler.shutdownNow();
            }
            this.socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendHeartbeat() {
        sendPacket(new Heartbeat().serialize());
    }

    public void sendBlockUpdate(Vector3i blockPos, BlockType blockType) {
        BlockUpdate request = new BlockUpdate(blockPos.x, blockPos.y, blockPos.z, blockType);
        sendPacket(request.serialize());
        System.out.println("[NET] Send Block Update " + blockPos + ", " + blockType);
    }

    public void requestChunk(int chunkX, int chunkZ) {
        ChunkRequest request = new ChunkRequest(chunkX, chunkZ);
        sendPacket(request.serialize());
        System.out.println("[NET] Send Chunk Request " + chunkX + ", " + chunkZ);
    }

    protected void sendPacket(byte[] data) {
        DatagramPacket udpPacket = new DatagramPacket(data, data.length);
        try {
            udpPacket.setAddress(java.net.InetAddress.getByName(serverHost));
            udpPacket.setPort(serverPort);
            socket.send(udpPacket);
            sentPacketCount.incrementAndGet();
        } catch (Exception e) {
            if (!socket.isClosed()) {
                e.printStackTrace();
            }
        }
    }

    public String serverHost() {
        return serverHost;
    }

    public int serverPort() {
        return serverPort;
    }

    public long sentPacketCount() {
        return sentPacketCount.get();
    }

    public long receivedPacketCount() {
        return listener == null ? 0L : listener.receivedPacketCount();
    }
}
