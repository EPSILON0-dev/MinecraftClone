package com.ee.Client;

import org.joml.*;

import com.ee.Common.Block;
import com.ee.Common.BlockType;
import com.ee.Common.Chunk;
import com.ee.Common.Config;

import java.util.ArrayList;

public class ChunkRenderer extends Chunk {
    private ChunkMesh mesh;
    ArrayList<ChunkMeshVertex> vertices;
    ArrayList<Integer> indices;

    public ChunkRenderer(Chunk chunk) {
        super(chunk.worldPosition(), chunk.getBlockArrayClone());
        this.mesh = null;
        this.vertices = new ArrayList<>();
        this.indices = new ArrayList<>();
    }

    public void generateMeshData() {
        this.vertices = new ArrayList<>();
        this.indices = new ArrayList<>();
        generateBlockMeshes(vertices, indices);
    }

    public void generateMesh() {
        mesh = new ChunkMesh(vertices, indices);
        System.out.println("[RENDER] Gen GL mesh, pos: " + worldPosition);
    }

    public ChunkMesh mesh() {
        return mesh;
    }

    public Matrix4f modelMatrix() {
        return new Matrix4f().translate(worldPosition.x * Config.CHUNK_SIZE.x, 0,
                worldPosition.y * Config.CHUNK_SIZE.z);
    }

    public boolean isReadyToRender() {
        return mesh != null;
    }

    private void generateBlockMeshes(ArrayList<ChunkMeshVertex> vertices, ArrayList<Integer> indices) {
        for (int x = 0; x < Config.CHUNK_SIZE.x; x++) {
            for (int y = 0; y < Config.CHUNK_SIZE.y; y++) {
                for (int z = 0; z < Config.CHUNK_SIZE.z; z++) {
                    GenerateBlockMesh(new Vector3i(x, y, z), vertices, indices);
                }
            }
        }
        System.out.println(
                "[RENDER] Gen mesh data, pos: " + worldPosition + ", vert: " + vertices.size() + ", tri: "
                        + indices.size() / 3);
    }

    private void GenerateBlockMesh(Vector3i position, ArrayList<ChunkMeshVertex> vertices, ArrayList<Integer> indices) {
        BlockType block = getBlock(position).type;

        if (block == BlockType.Air) {
            return;
        }

        Vector3f offset = new Vector3f(position);

        if (position.x <= 0 || getBlock(new Vector3i(position).sub(new Vector3i(1, 0, 0))).type == BlockType.Air) {
            int indexOffset = vertices.size();
            vertices.addAll(Cube.leftSide(offset, Block.getTextureIndex(block, BlockSide.Left)));
            indices.addAll(Cube.indices(indexOffset));
        }

        if (position.x >= Config.CHUNK_SIZE.x - 1
                || getBlock(new Vector3i(position).add(new Vector3i(1, 0, 0))).type == BlockType.Air) {
            int indexOffset = vertices.size();
            vertices.addAll(Cube.rightSide(offset, Block.getTextureIndex(block, BlockSide.Right)));
            indices.addAll(Cube.indices(indexOffset));
        }

        if (position.y <= 0 || getBlock(new Vector3i(position).sub(new Vector3i(0, 1, 0))).type == BlockType.Air) {
            int indexOffset = vertices.size();
            vertices.addAll(Cube.bottomSide(offset, Block.getTextureIndex(block, BlockSide.Bottom)));
            indices.addAll(Cube.indices(indexOffset));
        }

        if (position.y >= Config.CHUNK_SIZE.y - 1
                || getBlock(new Vector3i(position).add(new Vector3i(0, 1, 0))).type == BlockType.Air) {
            int indexOffset = vertices.size();
            vertices.addAll(Cube.topSide(offset, Block.getTextureIndex(block, BlockSide.Top)));
            indices.addAll(Cube.indices(indexOffset));
        }

        if (position.z <= 0 || getBlock(new Vector3i(position).sub(new Vector3i(0, 0, 1))).type == BlockType.Air) {
            int indexOffset = vertices.size();
            vertices.addAll(Cube.backSide(offset, Block.getTextureIndex(block, BlockSide.Back)));
            indices.addAll(Cube.indices(indexOffset));
        }

        if (position.z >= Config.CHUNK_SIZE.z - 1
                || getBlock(new Vector3i(position).add(new Vector3i(0, 0, 1))).type == BlockType.Air) {
            int indexOffset = vertices.size();
            vertices.addAll(Cube.frontSide(offset, Block.getTextureIndex(block, BlockSide.Front)));
            indices.addAll(Cube.indices(indexOffset));
        }
    }
}
