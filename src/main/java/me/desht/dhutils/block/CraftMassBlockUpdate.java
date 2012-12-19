package me.desht.dhutils.block;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_4_5.Chunk;
import net.minecraft.server.v1_4_5.ChunkCoordIntPair;
import net.minecraft.server.v1_4_5.World;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_4_5.CraftWorld;
import org.bukkit.craftbukkit.v1_4_5.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class CraftMassBlockUpdate implements MassBlockUpdate {
	private final World world;

	private int minX = Integer.MAX_VALUE;
	private int minZ = Integer.MAX_VALUE;
	private int maxX = Integer.MIN_VALUE;
	private int maxZ = Integer.MIN_VALUE;
	private int blocksModified = 0;

	public CraftMassBlockUpdate(org.bukkit.World world) {
		this.world = ((CraftWorld) world).getHandle();
	}

	@Override
	public boolean setBlock(int x, int y, int z, int blockId) {
		return setBlock(x, y, z, blockId, 0);
	}

	@Override
	public boolean setBlock(int x, int y, int z, int blockId, int data) {
		Chunk chunk = world.getChunkAt(x >> 4, z >> 4);

		minX = Math.min(minX, x);
		minZ = Math.min(minZ, z);
		maxX = Math.max(maxX, x);
		maxZ = Math.max(maxZ, z);

		blocksModified++;
		return chunk.a(x & 15, y, z & 15, blockId, data);
	}

	private List<ChunkCoordIntPair> calculateChunks() {
		List<ChunkCoordIntPair> res = new ArrayList<ChunkCoordIntPair>();
		if (blocksModified == 0) {
			return res;
		}
		int x1 = minX >> 4; int x2 = maxX >> 4;
		int z1 = minZ >> 4; int z2 = maxZ >> 4;
		for (int x = x1; x <= x2; x ++) {
			for (int z = z1; z <= z2; z ++) {
				res.add(new ChunkCoordIntPair(x, z));
			}	
		}
		return res;
	}

	private void recalculateLighting(List<ChunkCoordIntPair> affected) {
		for (ChunkCoordIntPair pair : affected) {
			Chunk c = world.getChunkAt(pair.x, pair.z);
			c.initLighting();
		}
	}

	@SuppressWarnings("unchecked")
	private void sendClientChanges(List<ChunkCoordIntPair> affected) {
		// A player is considered within viewing distance of the change if they're
		// within the change's bounding box expanded by the server view distance.
		int threshold = (Bukkit.getServer().getViewDistance() << 4);
		int x1 = minX - threshold, x2 = maxX + threshold;
		int z1 = minZ - threshold, z2 = maxZ + threshold;

		for (Player player : world.getWorld().getPlayers()) {
			Location loc = player.getLocation();
			int px = loc.getBlockX();
			int pz = loc.getBlockZ();
			if (px >= x1 && px <= x2 && pz >= z1 && pz <= z2) {
				for (ChunkCoordIntPair pair : affected) {
					((CraftPlayer) player).getHandle().chunkCoordIntPairQueue.add(pair);
				}
			}
		}
	}

	@Override
	public void notifyClients() {
		List<ChunkCoordIntPair> affectedChunks = calculateChunks();
		if (!affectedChunks.isEmpty()) {
//			recalculateLighting(affectedChunks);
			sendClientChanges(affectedChunks);
			blocksModified = 0;
			minX = minZ = Integer.MIN_VALUE;
			maxX = maxZ = Integer.MAX_VALUE;
		}
	}

	/**
	 * TODO: this should ideally be a method in the Bukkit World class, e.g world.createMassBlockUpdater()
	 * 
	 * @param world
	 * @return
	 */
	public static MassBlockUpdate createMassBlockUpdater(org.bukkit.World world) {
		return new CraftMassBlockUpdate(world);
	}
}