package me.desht.dhutils.block;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftChunk;

public class BlockUtils {

	/**
	 * Use direct NMS call to set a block without the lighting calculation overhead.
	 * The call is responsible for ensuring lighting is properly re-initialised afterwards.
	 * 
	 * @param b
	 * @param typeId
	 * @return
	 */
	public static boolean setBlockFast(Block b, int typeId) {
		Chunk c = b.getChunk();
		net.minecraft.server.Chunk chunk = ((CraftChunk) c).getHandle();
		return chunk.a(b.getX() & 15, b.getY(), b.getZ() & 15, typeId);
	}

	public static boolean setBlockFast(Block b, int typeId, byte data) {
		Chunk c = b.getChunk();
		net.minecraft.server.Chunk chunk = ((CraftChunk) c).getHandle();
		return chunk.a(b.getX() & 15, b.getY(), b.getZ() & 15, typeId, data);
	}

}
