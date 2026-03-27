package com.ee.Client;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import com.ee.Common.Network.*;

public class NetworkManager implements AutoCloseable {
    private DatagramSocket socket;
    private NetworkListener listener;
    private ClientWorld world;

    public NetworkManager(ClientWorld world) {
        this.world = world;
        try {
            this.socket = new DatagramSocket();
            this.listener = new NetworkListener(this.socket, this.world);
            new Thread(this.listener).start();
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

    public void sendHeartbeat() {
    }

    public void sendBlockUpdate(int x, int y, int z, int blockType) {
    }

    public void requestChunk(int chunkX, int chunkZ) {
        ChunkRequest request = new ChunkRequest(chunkX, chunkZ);
        byte[] requestData = request.serialize();
        DatagramPacket udpPacket = new DatagramPacket(requestData, requestData.length);
        try {
            // TODO hardcoded for now
            udpPacket.setAddress(java.net.InetAddress.getByName("localhost"));
            udpPacket.setPort(6767);
            socket.send(udpPacket);
            System.out.println("[NET] Request " + chunkX + ", " + chunkZ);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
