package me.desht.dhutils.block;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.Chunk;
import net.minecraft.server.ChunkCoordIntPair;
import net.minecraft.server.World;

public class CraftMassBlockUpdate implements MassBlockUpdate {
	private final World world;
	private final Set<ChunkCoordIntPair> affectedChunks = new HashSet<ChunkCoordIntPair>();

	private int minX = Integer.MAX_VALUE;
	private int minZ = Integer.MAX_VALUE;
	private int maxX = Integer.MIN_VALUE;
	private int maxZ = Integer.MIN_VALUE;

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
		affectedChunks.add(new ChunkCoordIntPair(x >> 4, z >> 4));

		minX = Math.min(minX, x);
		minZ = Math.min(minZ, z);
		maxX = Math.max(maxX, x);
		maxZ = Math.max(maxZ, z);

		return chunk.a(x & 15, y, z & 15, blockId, data);
	}

	private void recalculateLighting() {
		for (ChunkCoordIntPair pair : affectedChunks) {
			Chunk c = world.getChunkAt(pair.x, pair.z);
			c.initLighting();
		}
	}

	@SuppressWarnings("unchecked")
	private void sendClientChanges() {
		if (affectedChunks.isEmpty()) {
			return;
		}

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
				for (ChunkCoordIntPair pair : affectedChunks) {
					((CraftPlayer) player).getHandle().chunkCoordIntPairQueue.add(pair);
				}
			}
		}
	}

	@Override
	public void notifyClients() {
		recalculateLighting();
		sendClientChanges();
		affectedChunks.clear();
		minX = minZ = Integer.MIN_VALUE;
		maxX = maxZ = Integer.MAX_VALUE;
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
