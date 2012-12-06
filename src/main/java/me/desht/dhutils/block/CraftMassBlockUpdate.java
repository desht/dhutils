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

	private int minX = Integer.MIN_VALUE;
	private int minZ = Integer.MIN_VALUE;
	private int maxX = Integer.MAX_VALUE;
	private int maxZ = Integer.MAX_VALUE;

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

		minX = Math.max(minX, x);
		minZ = Math.max(minZ, z);
		maxX = Math.min(maxX, x);
		maxZ = Math.min(maxZ, z);

		return chunk.a(x & 15, y, z, blockId, data);
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

		int threshold = (Bukkit.getServer().getViewDistance() << 4) + 32;
		threshold = threshold * threshold;

		int centerX = minX + (maxX - minX) / 2;
		int centerZ = minZ + (maxZ - minZ) / 2;

		for (Player player : world.getWorld().getPlayers()) {
			Location loc = player.getLocation();
			int px = loc.getBlockX();
			int pz = loc.getBlockZ();
			if ((px - centerX) * (px - centerX) + (pz - centerZ) * (pz - centerZ) < threshold) {
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
