package com.ee.Client;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

import com.ee.Common.*;

public class ImGuiOverlay implements AutoCloseable {
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    public void init(long windowHandle) {
        ImGui.createContext();

        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);

        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init("#version 330 core");
    }

    public void render(float fps, Player player, ClientWorld world, NetworkManager networkManager) {
        imGuiGl3.newFrame();
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        debugWindow(fps, player, world, networkManager);
        crosshairWindow();

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    private void crosshairWindow() {
        // Hacky way to get an X in the center :3
        ImGui.setNextWindowPos(ImGui.getIO().getDisplaySizeX() / 2.0f, ImGui.getIO().getDisplaySizeY() / 2.0f,
                ImGuiCond.Always, 0.5f, 0.5f);
        ImGui.begin("Crosshair",
                ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove
                        | ImGuiWindowFlags.NoScrollbar
                        | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoBackground);
        ImGui.setWindowFontScale(3.0f);
        ImGui.text("+");
        ImGui.end();
    }

    private static int prevChunkHash = 0;
    private static int chunkCompressedSize = 0;

    private void debugWindow(float fps, Player player, ClientWorld world, NetworkManager networkManager) {
        ImGui.setNextWindowPos(16.0f, 16.0f, ImGuiCond.Once);
        ImGui.setNextWindowSize(320.0f, 360.0f, ImGuiCond.Once);
        ImGui.begin("Debug Menu", ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoCollapse);
        ImGui.text(String.format("FPS: %.1f", fps));

        ImGui.separator();

        var position = player.position();
        var direction = player.direction();
        var velocity = player.velocity();
        var selectedBlockType = player.selectedBlockType();
        ImGui.text(String.format("Position: %.2f, %.2f, %.2f", position.x, position.y, position.z));
        ImGui.text(String.format("Direction: %.2f, %.2f, %.2f", direction.x, direction.y, direction.z));
        ImGui.text(String.format("Velocity: %.2f, %.2f, %.2f", velocity.x, velocity.y, velocity.z));
        
        ImGui.separator();
        
        var chunkPos = ClientWorld.getChunkInWorld(Util.vec3fToVec3i(player.position()));
        var chunk = world.getChunk(chunkPos);
        int hash = chunk != null ? chunk.computeHash() : 0;
        ImGui.text(String.format("Chunk: %d, %d", chunkPos.x, chunkPos.y));
        ImGui.text(String.format("Hash: %d", hash));
        if (hash != prevChunkHash) {
            chunkCompressedSize = chunk != null ? new CompressedChunk(chunk).getCompressedSize() : 0;
        }
        ImGui.text(String.format("Compressed: %d bytes", chunkCompressedSize));
        prevChunkHash = hash;

        ImGui.separator();
        ImGui.text(String.format("Loaded Chunks: %d", world.loadedChunkCount()));
        ImGui.text(String.format("Server: %s:%d", networkManager.serverHost(), networkManager.serverPort()));
        ImGui.text(String.format("Packets Sent: %d", networkManager.sentPacketCount()));
        ImGui.text(String.format("Packets Received: %d", networkManager.receivedPacketCount()));
        
        ImGui.separator();
        ImGui.text(String.format("Selected Block: %s", selectedBlockType));
        ImGui.end();
    }

    @Override
    public void close() {
        imGuiGl3.shutdown();
        imGuiGlfw.shutdown();
        ImGui.destroyContext();
    }
}
