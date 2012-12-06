package me.desht.dhutils.block;

public interface MassBlockUpdate {
	/**
	 * Makes a fast block change at the given coordinates
	 *
	 * @param x X-coordinate of the block
	 * @param y Y-coordinate of the block
	 * @param z Z-coordinate of the block
	 * @param blockId the new Block ID
	 * @return whether the block was actually changed
	 */
	public boolean setBlock(int x, int y, int z, int blockId);

	/**
	 * Makes a fast block change at the given coordinates
	 *
	 * @param x X-coordinate of the block
	 * @param y Y-coordinate of the block
	 * @param z Z-coordinate of the block
	 * @param blockId the new Block ID
	 * @param data the new Block data
	 * @return whether the block was actually changed
	 */
	public boolean setBlock(int x, int y, int z, int blockId, int data);

	/**
	 * Recalculate lighting on all chunks affected by this mass block update, and re-send any altered chunks
	 * to all players within viewing distance of the change.
	 */
	public void notifyClients();
}
