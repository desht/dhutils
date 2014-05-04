package me.desht.dhutils.block;

import me.desht.dhutils.nms.NMSHelper;
import me.desht.dhutils.nms.api.NMSAbstraction;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class CraftMassBlockUpdate implements MassBlockUpdate, Runnable {
	private final Plugin plugin;
	private final World world;
	private final NMSAbstraction nms;

	private RelightingStrategy relightingStrategy = RelightingStrategy.IMMEDIATE;

	private static final int MAX_BLOCKS_PER_TIME_CHECK = 1000;
	private Queue<DeferredBlock> deferredBlocks = new ArrayDeque<DeferredBlock>();
	private BukkitTask relightTask = null;
	private long maxRelightTimePerTick = TimeUnit.NANOSECONDS.convert(1, TimeUnit.MILLISECONDS);

	private int minX = Integer.MAX_VALUE;
	private int minZ = Integer.MAX_VALUE;
	private int maxX = Integer.MIN_VALUE;
	private int maxZ = Integer.MIN_VALUE;
	private int blocksModified = 0;

	public CraftMassBlockUpdate(Plugin plugin, org.bukkit.World world) {
		this.plugin = plugin;
		this.world = world;
		this.nms = NMSHelper.getNMS();
		if (nms == null) {
			throw new IllegalStateException("NMS abstraction API is not available");
		}
	}

	public boolean setBlock(int x, int y, int z, int blockId) {
		return setBlock(x, y, z, blockId, 0);
	}

	public boolean setBlock(int x, int y, int z, int blockId, int data) {
		minX = Math.min(minX, x);
		minZ = Math.min(minZ, z);
		maxX = Math.max(maxX, x);
		maxZ = Math.max(maxZ, z);

		blocksModified++;
		int oldBlockId = world.getBlockTypeIdAt(x, y, z);
		boolean res = nms.setBlockFast(world, x, y, z, blockId, (byte)data);

		if (relightingStrategy != RelightingStrategy.NEVER) {
			if (nms.getBlockLightBlocking(oldBlockId) != nms.getBlockLightBlocking(blockId)
					|| nms.getBlockLightEmission(oldBlockId) != nms.getBlockLightEmission(blockId)) {
				// lighting or light blocking by this block has changed; force a recalculation
				if (relightingStrategy == RelightingStrategy.IMMEDIATE) {
					nms.recalculateBlockLighting(world, x, y, z);
				} else if (relightingStrategy == RelightingStrategy.DEFERRED || relightingStrategy == RelightingStrategy.HYBRID) {
					deferredBlocks.add(new DeferredBlock(x, y, z));
				}
			}
		}
		return res;
	}

	public void notifyClients() {
		if (relightingStrategy == RelightingStrategy.DEFERRED || relightingStrategy == RelightingStrategy.HYBRID) {
			relightTask = Bukkit.getScheduler().runTaskTimer(plugin, this, 1L, 1L);
		}
        if (relightingStrategy != RelightingStrategy.DEFERRED) {
			for (ChunkCoords cc : calculateChunks()) {
				world.refreshChunk(cc.x, cc.z);
			}
		}
	}

	public void run() {
		long now = System.nanoTime();
		int n = 1;

		while (deferredBlocks.peek() != null) {
			DeferredBlock db = deferredBlocks.poll();
            // Don't consider blocks that are completely surrounded by other non-transparent blocks
            if (canAffectLighting(world, db.x, db.y, db.z)) {
                nms.recalculateBlockLighting(world, db.x, db.y, db.z);
                if (n++ % MAX_BLOCKS_PER_TIME_CHECK == 0) {
                    if (System.nanoTime() - now > maxRelightTimePerTick) {
                        break;
                    }
                }
            }
		}

		if (deferredBlocks.isEmpty()) {
			relightTask.cancel();
			relightTask = null;
            Set<ChunkCoords> touched = calculateChunks();
            System.out.println("Refreshing chunks: " + touched.size());
			for (ChunkCoords cc : touched) {
				world.refreshChunk(cc.x, cc.z);
			}
		}
	}

	public void setRelightingStrategy(RelightingStrategy strategy) {
		this.relightingStrategy = strategy;
	}

	public void setMaxRelightTimePerTick(long value, TimeUnit timeUnit) {
		maxRelightTimePerTick = timeUnit.toNanos(value);
	}

	public int getBlocksToRelight() {
		return deferredBlocks.size();
	}

	public void setDeferredBufferSize(int size) {
		if (!deferredBlocks.isEmpty()) {
			// resizing an existing buffer is not supported
			throw new IllegalStateException("setDeferredBufferSize() called after block updates made");
		}
		if (relightingStrategy != RelightingStrategy.DEFERRED && relightingStrategy != RelightingStrategy.HYBRID) {
			// reduce accidental memory wastage if called when not needed
			throw new IllegalStateException("setDeferredBufferSize() called when relighting strategy not DEFERRED or HYBRID");
		}
		deferredBlocks = new ArrayDeque<CraftMassBlockUpdate.DeferredBlock>(size);
	}

    private boolean canAffectLighting(World world, int x, int y, int z) {
        Block base  = world.getBlockAt(x, y, z);
        Block east  = base.getRelative(BlockFace.EAST);
        Block west  = base.getRelative(BlockFace.WEST);
        Block up    = base.getRelative(BlockFace.UP);
        Block down  = base.getRelative(BlockFace.DOWN);
        Block south = base.getRelative(BlockFace.SOUTH);
        Block north = base.getRelative(BlockFace.NORTH);

        return east.getType().isTransparent() ||
                west.getType().isTransparent() ||
                up.getType().isTransparent() ||
                down.getType().isTransparent() ||
                south.getType().isTransparent() ||
                north.getType().isTransparent();
    }

	private Set<ChunkCoords> calculateChunks() {
		Set<ChunkCoords> res = new HashSet<ChunkCoords>();
		if (blocksModified == 0) {
			return res;
		}
		int x1 = minX >> 4; int x2 = maxX >> 4;
		int z1 = minZ >> 4; int z2 = maxZ >> 4;
		for (int x = x1; x <= x2; x++) {
			for (int z = z1; z <= z2; z++) {
				res.add(new ChunkCoords(x, z));
			}
		}
		return res;
	}

	/**
	 * TODO: this should be a method in the Bukkit CraftWorld class, e.g world.createMassBlockUpdate()
	 *
	 * @param world
	 * @return
	 */
	public static MassBlockUpdate createMassBlockUpdater(Plugin plugin, org.bukkit.World world) {
		return new CraftMassBlockUpdate(plugin, world);
	}

	private class ChunkCoords {
		public final int x, z;
		public ChunkCoords(int x, int z) {
			this.x = x;
			this.z = z;
		}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ChunkCoords that = (ChunkCoords) o;

            if (x != that.x) return false;
            if (z != that.z) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + z;
            return result;
        }
    }

	private class DeferredBlock {
		public final int x, y, z;

		public DeferredBlock(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
}