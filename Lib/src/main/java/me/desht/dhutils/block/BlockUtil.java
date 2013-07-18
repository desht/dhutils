package me.desht.dhutils.block;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class BlockUtil {
	private static final BlockFace[] allFaces = new BlockFace[] {
		BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.DOWN, BlockFace.UP
	};

	/**
	 * Get the nearest face on the given block relative to the given location.
	 *
	 * @param block
	 * @param loc
	 * @return
	 */
	public static BlockFace getNearestFace(Block block, Location loc) {
		Location bLoc = block.getLocation().add(0.5, 0.5, 0.5);
		Vector v = loc.toVector().subtract(bLoc.toVector());
		float min = Float.MAX_VALUE;
		BlockFace wantedFace = null;
		for (BlockFace face : allFaces) {
			Vector v1 = new Vector(face.getModX(), face.getModY(), face.getModZ());
			float angle = v.angle(v1);
			if (angle < min) {
				min = angle;
				wantedFace = face;
			}
		}
		return wantedFace;
	}
}
