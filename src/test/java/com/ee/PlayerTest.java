package com.ee;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.junit.jupiter.api.Test;

import com.ee.Client.Camera;
import com.ee.Client.Player;
import com.ee.Client.ClientWorld;
import com.ee.Common.Block;
import com.ee.Common.BlockType;
import com.ee.Common.Chunk;
import com.ee.Common.Config;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlayerTest {

	@Test
	public void rotateClampsPitchAndKeepsDirectionNormalized() {
		Player player = new Player(new Vector3f(0.5f, 1.0f, 0.5f), new Vector3f(1.0f, 0.0f, 0.0f));

		player.rotate(0.0f, (float) Math.toRadians(120.0f));

		assertEquals(1.0f, player.direction().length(), 0.0001f);
		assertEquals((float) Math.sin(Math.toRadians(89.0f)), player.direction().y, 0.0001f);
		assertEquals((float) Math.cos(Math.toRadians(89.0f)), player.direction().x, 0.0001f);
		assertEquals(0.0f, player.direction().z, 0.0001f);
	}

	@Test
	public void jumpOnlyAppliesImpulseWhenGrounded() {
		ClientWorld world = createFloorWorld();
		Player grounded = new Player(new Vector3f(0.5f, 1.0f, 0.5f), new Vector3f(1.0f, 0.0f, 0.0f));
		Player airborne = new Player(new Vector3f(0.5f, 3.0f, 0.5f), new Vector3f(1.0f, 0.0f, 0.0f));

		grounded.jump(world);
		airborne.jump(world);

		assertEquals(Config.PLAYER_JUMP_IMPULSE, grounded.velocity().y, 0.0001f);
		assertEquals(0.0f, airborne.velocity().y, 0.0001f);
	}

	@Test
	public void setupCameraUsesEyeHeightOffsetAndPlayerDirection() {
		Player player = new Player(new Vector3f(2.0f, 4.0f, 6.0f), new Vector3f(0.0f, 0.0f, 1.0f));
		Camera camera = new Camera(new Vector3f(), new Vector3f(1.0f, 0.0f, 0.0f), 90.0f, 1.0f);

		player.setupCamera(camera);

		assertEquals(2.0f, camera.position().x, 0.0001f);
		assertEquals(5.6f, camera.position().y, 0.0001f);
		assertEquals(6.0f, camera.position().z, 0.0001f);
		assertEquals(player.direction().x, camera.direction().x, 0.0001f);
		assertEquals(player.direction().y, camera.direction().y, 0.0001f);
		assertEquals(player.direction().z, camera.direction().z, 0.0001f);
	}

	private static ClientWorld createEmptyWorld() {
		ClientWorld world = new ClientWorld();
		fillWorld(world, BlockType.Air);
		return world;
	}

	private static ClientWorld createFloorWorld() {
		ClientWorld world = createEmptyWorld();
		for (int x = 0; x < Config.CHUNK_SIZE.x; x++) {
			for (int z = 0; z < Config.CHUNK_SIZE.z; z++) {
				world.setBlock(new Vector3i(x, 0, z), new Block(BlockType.Cobblestone));
			}
		}
		return world;
	}

	private static void fillWorld(ClientWorld world, BlockType blockType) {
		world.addChunk(new Vector2i(0, 0), new Chunk(new Vector2i(0, 0)));
		for (int x = 0; x < Config.CHUNK_SIZE.x; x++) {
			for (int y = 0; y < Config.CHUNK_SIZE.y; y++) {
				for (int z = 0; z < Config.CHUNK_SIZE.z; z++) {
					world.setBlock(new Vector3i(x, y, z), new Block(blockType));
				}
			}
		}
	}

}
