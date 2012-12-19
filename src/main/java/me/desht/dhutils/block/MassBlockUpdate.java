package me.desht.dhutils.block;

public interface MassBlockUpdate {
	public enum RelightingStrategy {
		/**
		 * Do not do any re-lighting calculations at all.  If any block lighting properties (i.e. light emission or
		 * light blocking) change, this may result in incorrect lighting of the changed blocks.  This strategy should
		 * only beused if you are certain that no lighting 
		 * properties are being changed, or your plugin will handle re-lighting itself.
		 */
		NEVER,
		
		/**
		 * Immediately re-light any blocks whose lighting properties have changed.  For very large changes, this
		 * may result in some server lag.
		 */
		IMMEDIATE,
		
		/**
		 * Re-light any blocks whose lighting properties have changed over the course of the next several server
		 * ticks, attempting to minimise server lag.
		 */
		DEFERRED;
	};

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

	/**
	 * Set the block re-lighting strategy for this mass block update.  The default strategy is 
	 * RelightingStrategy.IMMEDIATE.
	 * 
	 * @param strategy	the desired re-lighting strategy
	 */
	public void setRelightingStrategy(RelightingStrategy strategy);
}
