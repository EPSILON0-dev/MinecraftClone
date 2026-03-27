package com.ee.Server;

import org.joml.Vector2i;

public class Main {
    private static int port = 6767;
    private static Listener listener;
    private static ServerWorld world;

    public static void main(String[] args) {

        // Create the server world and generate some chunks around spawn
        world = new ServerWorld();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                world.getOrGenerateChunk(new Vector2i(x, z));
            }
        }

        listener = new Listener(port, world);

        Thread listenerThread = new Thread(listener);
        listenerThread.start();

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
