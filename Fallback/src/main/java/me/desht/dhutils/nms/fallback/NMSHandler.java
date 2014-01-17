package me.desht.dhutils.nms.fallback;

import org.bukkit.World;
import org.bukkit.entity.Player;

import me.desht.dhutils.nms.api.NMSAbstraction;

public class NMSHandler implements NMSAbstraction {

	@Override
	public boolean setBlockFast(World world, int x, int y, int z, int blockId, byte data) {
		return world.getBlockAt(x, y, z).setTypeIdAndData(blockId, data, false);
	}

	@Override
	public void forceBlockLightLevel(World world, int x, int y, int z, int level) {
	}

	@Override
	public int getBlockLightEmission(int blockId) {
		return 0;
	}

	@Override
	public int getBlockLightBlocking(int blockId) {
		return 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void queueChunkForUpdate(Player player, int cx, int cz) {
		player.getWorld().refreshChunk(cx, cz);
	}

	@Override
	public void recalculateBlockLighting(World world, int x, int y, int z) {
	}
}
