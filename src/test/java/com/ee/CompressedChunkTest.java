package com.ee;

import org.joml.Vector2i;
import org.joml.Vector3i;
import org.junit.jupiter.api.Test;

import com.ee.Common.Block;
import com.ee.Common.BlockType;
import com.ee.Common.Chunk;
import com.ee.Common.CompressedChunk;
import com.ee.Common.Config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CompressedChunkTest {

	@Test
	public void compressAndDecompressPreservesMixedChunkContents() {
		Chunk original = new Chunk(new Vector2i(3, -2));
		original.setBlock(new Vector3i(0, 0, 0), new Block(BlockType.Bedrock));
		original.setBlock(new Vector3i(1, 2, 3), new Block(BlockType.Dirt));
		original.setBlock(new Vector3i(4, 5, 6), new Block(BlockType.Air));
		original.setBlock(new Vector3i(7, 8, 9), new Block(BlockType.OakLeaves));
		original.setBlock(new Vector3i(10, 11, 12), new Block(BlockType.Stone));

		CompressedChunk compressed = new CompressedChunk(original);
		Chunk decompressed = compressed.decompress();

		assertEquals(original.worldPosition(), decompressed.worldPosition());
		assertEquals(original.computeHash(), decompressed.computeHash());
		assertChunkContentsEqual(original, decompressed);
	}

	@Test
	public void compressAndDecompressPreservesGeneratedChunkContents() {
		Chunk original = new Chunk(new Vector2i(-4, 9));
		original.generateBlocks();

		CompressedChunk compressed = new CompressedChunk(original);
		Chunk decompressed = compressed.decompress();

		assertEquals(original.worldPosition(), decompressed.worldPosition());
		assertEquals(original.computeHash(), decompressed.computeHash());
		assertChunkContentsEqual(original, decompressed);
	}

	@Test
	public void decompressRejectsCorruptedCompressedData() {
		Chunk original = new Chunk(new Vector2i(1, 1));
		original.generateBlocks();
		CompressedChunk compressed = new CompressedChunk(original);
		byte[] corruptedData = compressed.compressedData().clone();

		for (int i = 0; i < Math.min(8, corruptedData.length); i++) {
			corruptedData[i] ^= (byte) 0xFF;
		}

		CompressedChunk corrupted = new CompressedChunk(compressed.worldPosition(), compressed.hash(), corruptedData);

		assertThrows(IllegalStateException.class, corrupted::decompress);
	}

	@Test
	public void decompressRejectsHashMismatch() {
		Chunk original = new Chunk(new Vector2i(2, 5));
		original.generateBlocks();
		CompressedChunk compressed = new CompressedChunk(original);

		CompressedChunk tampered = new CompressedChunk(
				compressed.worldPosition(),
				compressed.hash() + 1,
				compressed.compressedData().clone());

		assertThrows(IllegalStateException.class, tampered::decompress);
	}

	private static void assertChunkContentsEqual(Chunk expected, Chunk actual) {
		assertEquals(expected.worldPosition().x, actual.worldPosition().x);
		assertEquals(expected.worldPosition().y, actual.worldPosition().y);

		Block[] expectedBlocks = expected.getBlockArrayClone();
		Block[] actualBlocks = actual.getBlockArrayClone();
		assertEquals(Config.CHUNK_BLOCK_COUNT, actualBlocks.length);

		for (int i = 0; i < expectedBlocks.length; i++) {
			Block expectedBlock = expectedBlocks[i];
			Block actualBlock = actualBlocks[i];

			if (expectedBlock == null) {
				assertNull(actualBlock, "Expected null block at index " + i);
				continue;
			}

			assertNotNull(actualBlock, "Expected non-null block at index " + i);
			assertEquals(expectedBlock.type, actualBlock.type, "Mismatched block type at index " + i);
		}
	}
}
