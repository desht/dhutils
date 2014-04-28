package me.desht.dhutils.nms.api;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public interface NMSAbstraction {
	/**
	 * Update the low-level chunk information for the given block to the new block ID and data.  This
	 * change will not be propagated to clients until the chunk is refreshed to them (e.g. by the
	 * Bukkit world.refreshChunk() method).  The block's light level will also not be recalculated,
	 * nor will the light level of any nearby blocks which might be affected by the change in this
	 * block.
	 *
	 * @param world the world
	 * @param x X co-ordinate of the block to change
	 * @param y Y co-ordinate of the block to change
	 * @param z Z co-ordinate of the block to change
	 * @param blockId the new block ID
	 * @param data the block data
	 * @return true if the change was made, false otherwise
	 */
	public boolean setBlockFast(World world, int x, int y, int z, int blockId, byte data);

	/**
	 * Force an override of the light level at the given block position
	 *
	 * @param world the world
	 * @param x X co-ordinate of the block to relight
	 * @param y Y co-ordinate of the block to relight
	 * @param z Z co-ordinate of the block to relight
	 * @param level the new light level
	 */
	public void forceBlockLightLevel(World world, int x, int y, int z, int level);

	/**
	 * Force a recalculation of the "natural" light level at the given block position
	 *
	 * @param world the world
	 * @param x X co-ordinate of the block to relight
	 * @param y Y co-ordinate of the block to relight
	 * @param z Z co-ordinate of the block to relight
	 */
	public void recalculateBlockLighting(World world, int x, int y, int z);

	/**
	 * Get the light emission level for the given block.
	 *
	 * @param blockId numeric ID of the block to check
	 * @return the block's light emission level
	 */
	public int getBlockLightEmission(int blockId);

	/**
	 * Get the light blocking amount of the given block, i.e. the amount of light lost when
	 * passing through the block.
	 *
	 * @param blockId numeric ID of the block to check
	 * @return the block's light blocking level
	 */
	public int getBlockLightBlocking(int blockId);

	/**
	 * Queue the given chunk up for updating to the player.
	 *
	 * @param player the player
	 * @param cx the chunk's X co-ordinate
	 * @param cz the chunk's Z co-ordinate
	 * @deprecated use Bukkit's world.refreshChunk() method
	 */
	@Deprecated
	public void queueChunkForUpdate(Player player, int cx, int cz);

	/**
	 * Get the actual hitbox of the given block, which may be smaller than the block's full boundary.
	 * Two vectors are returned; the first vector represents the corner of the hitbox with minimum X, Y
	 * and Z values, and the second vector represents the corner with maximum X, Y, and Z value.
	 *
	 * @param block the block to check
	 * @return a pair of vectors representing two opposite corners of the hitbox
	 */
	public Vector[] getBlockHitbox(Block block);
}
