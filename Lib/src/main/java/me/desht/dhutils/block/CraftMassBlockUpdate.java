package me.desht.dhutils.block;

import java.util.ArrayList;
import java.util.List;

import me.desht.dhutils.nms.NMSHelper;
import me.desht.dhutils.nms.api.NMSAbstraction;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class CraftMassBlockUpdate implements MassBlockUpdate {
	private final World world;
	private final List<DeferredBlock> deferredBlocks = new ArrayList<DeferredBlock>();

	private int minX = Integer.MAX_VALUE;
	private int minZ = Integer.MAX_VALUE;
	private int maxX = Integer.MIN_VALUE;
	private int maxZ = Integer.MIN_VALUE;
	private int blocksModified = 0;
	private RelightingStrategy relightingStrategy = RelightingStrategy.IMMEDIATE;
	private final NMSAbstraction nms;

	public CraftMassBlockUpdate(org.bukkit.World world) {
		this.world = world;
		this.nms = NMSHelper.getNMS();
		if (nms == null) {
			throw new IllegalStateException("NMS abstraction API is not available");
		}
	}

	@Override
	public boolean setBlock(int x, int y, int z, int blockId) {
		return setBlock(x, y, z, blockId, 0);
	}

	@Override
	public boolean setBlock(int x, int y, int z, int blockId, int data) {
		minX = Math.min(minX, x);
		minZ = Math.min(minZ, z);
		maxX = Math.max(maxX, x);
		maxZ = Math.max(maxZ, z);

		blocksModified++;
		int oldBlockId = world.getBlockTypeIdAt(x & 0x0f, y, z & 0x0f);
		boolean res = nms.setBlockFast(world, x, y, z, blockId, (byte)data);
		
		if (nms.getBlockLightEmission(oldBlockId) != nms.getBlockLightEmission(blockId)
				|| nms.getBlockLightBlocking(oldBlockId) != nms.getBlockLightBlocking(oldBlockId)) {
			// lighting or light blocking by this block has changed; force a recalculation
			switch (relightingStrategy) {
			case IMMEDIATE:
				nms.recalculateBlockLighting(world, x, y, z);
				break;
			case DEFERRED:
				deferredBlocks.add(new DeferredBlock(x, y, z, blockId));
				break;
			}
		}
		return res;
	}

	private List<ChunkCoords> calculateChunks() {
		List<ChunkCoords> res = new ArrayList<ChunkCoords>();
		if (blocksModified == 0) {
			return res;
		}
		int x1 = minX >> 4; int x2 = maxX >> 4;
		int z1 = minZ >> 4; int z2 = maxZ >> 4;
		for (int x = x1; x <= x2; x ++) {
			for (int z = z1; z <= z2; z ++) {
				res.add(new ChunkCoords(x, z));
			}	
		}
		return res;
	}

	private void sendClientChanges(List<ChunkCoords> affected) {
		// A player is considered within viewing distance of the change if they're
		// within the change's bounding box expanded by the server view distance.
		int threshold = (Bukkit.getServer().getViewDistance() << 4);
		int x1 = minX - threshold, x2 = maxX + threshold;
		int z1 = minZ - threshold, z2 = maxZ + threshold;

		for (Player player : world.getPlayers()) {
			Location loc = player.getLocation();
			int px = loc.getBlockX();
			int pz = loc.getBlockZ();
			if (px >= x1 && px <= x2 && pz >= z1 && pz <= z2) {
				for (ChunkCoords pair : affected) {
					nms.queueChunkForUpdate(player, pair.x, pair.z);
				}
			}
		}
	}

	@Override
	public void notifyClients() {
		List<ChunkCoords> affectedChunks = calculateChunks();
		if (!affectedChunks.isEmpty()) {
			sendClientChanges(affectedChunks);
			blocksModified = 0;
			minX = minZ = Integer.MIN_VALUE;
			maxX = maxZ = Integer.MAX_VALUE;
		}
	}

	@Override
	public void setRelightingStrategy(RelightingStrategy strategy) {
		if (strategy == RelightingStrategy.DEFERRED) {
			throw new NotImplementedException("DEFERRED re-lighting strategy not yet supported");
		}
		this.relightingStrategy = strategy;
	}
	
	/**
	 * TODO: this should be a method in the Bukkit World class, e.g world.createMassBlockUpdater()
	 * 
	 * @param world
	 * @return
	 */
	public static MassBlockUpdate createMassBlockUpdater(org.bukkit.World world) {
		return new CraftMassBlockUpdate(world);
	}
	
	private class ChunkCoords {
		public final int x, z;
		public ChunkCoords(int x, int z) {
			this.x = x;
			this.z = z;
		}
	}
	
	private class DeferredBlock {
		public final int x, y, z;
		public final int blockId;
		
		public DeferredBlock(int x, int y, int z, int blockId) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.blockId = blockId;
		}
	}
}