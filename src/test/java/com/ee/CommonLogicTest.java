package com.ee;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.junit.jupiter.api.Test;

import com.ee.Client.BlockSide;
import com.ee.Common.Block;
import com.ee.Common.BlockTextures;
import com.ee.Common.BlockType;
import com.ee.Common.Util;

public class CommonLogicTest {

    @Test
    public void blockTypeNextSkipsAirAndWraps() {
        assertEquals(BlockType.Stone, BlockType.next(BlockType.Air));
        assertEquals(BlockType.Stone, BlockType.next(BlockType.OakLeaves));
    }

    @Test
    public void blockTypePreviousSkipsAirAndWraps() {
        assertEquals(BlockType.OakLeaves, BlockType.previous(BlockType.Stone));
        assertEquals(BlockType.OakLog, BlockType.previous(BlockType.OakLeaves));
    }

    @Test
    public void blockTextureIndexUsesSideSpecificMappings() {
        assertEquals(BlockTextures.GrassTop.ordinal(), Block.getTextureIndex(BlockType.Grass, BlockSide.Top));
        assertEquals(BlockTextures.Dirt.ordinal(), Block.getTextureIndex(BlockType.Grass, BlockSide.Bottom));
        assertEquals(BlockTextures.GrassSide.ordinal(), Block.getTextureIndex(BlockType.Grass, BlockSide.Left));
        assertEquals(BlockTextures.OakLogTop.ordinal(), Block.getTextureIndex(BlockType.OakLog, BlockSide.Top));
        assertEquals(BlockTextures.OakLogSide.ordinal(), Block.getTextureIndex(BlockType.OakLog, BlockSide.Front));
    }

    @Test
    public void lerpInterpolatesLinearly() {
        assertEquals(12.5f, Util.lerp(10.0f, 20.0f, 0.25f), 0.0001f);
    }

    @Test
    public void vec3fToVec3iFloorsEachComponent() {
        Vector3i floored = Util.vec3fToVec3i(new Vector3f(1.9f, -0.1f, -2.8f));

        assertEquals(new Vector3i(1, -1, -3), floored);
    }
}
