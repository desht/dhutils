package me.desht.dhutils.nms.api;

import org.bukkit.World;
import org.bukkit.entity.Player;

public interface NMSAbstraction {
	public boolean setBlockFast(World world, int x, int y, int z, int blockId,
			byte data);

	public void forceBlockLightLevel(World world, int x, int y, int z, int level);

	public void recalculateBlockLighting(World world, int x, int y, int z);

	public int getBlockLightEmission(int blockId);

	public int getBlockLightBlocking(int blockId);

	public void queueChunkForUpdate(Player player, int cx, int cz);

}
