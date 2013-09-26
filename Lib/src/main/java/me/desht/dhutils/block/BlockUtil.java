package me.desht.dhutils.block;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class BlockUtil {
	private static final BlockFace[] allFaces = new BlockFace[] {
		BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.DOWN, BlockFace.UP
	};

	private static Map<BlockFace,BlockFace> toLeft = new HashMap<BlockFace, BlockFace>();
	static {
		toLeft.put(BlockFace.NORTH, BlockFace.WEST);
		toLeft.put(BlockFace.NORTH_NORTH_EAST, BlockFace.NORTH_WEST);
		toLeft.put(BlockFace.NORTH_EAST, BlockFace.NORTH_WEST);
		toLeft.put(BlockFace.EAST_NORTH_EAST, BlockFace.NORTH_WEST);
		toLeft.put(BlockFace.EAST, BlockFace.NORTH);
		toLeft.put(BlockFace.EAST_SOUTH_EAST, BlockFace.NORTH_EAST);
		toLeft.put(BlockFace.SOUTH_EAST, BlockFace.NORTH_EAST);
		toLeft.put(BlockFace.SOUTH_SOUTH_EAST, BlockFace.NORTH_EAST);
		toLeft.put(BlockFace.SOUTH, BlockFace.EAST);
		toLeft.put(BlockFace.SOUTH_SOUTH_WEST, BlockFace.SOUTH_EAST);
		toLeft.put(BlockFace.SOUTH_WEST, BlockFace.SOUTH_EAST);
		toLeft.put(BlockFace.WEST_SOUTH_WEST, BlockFace.SOUTH_EAST);
		toLeft.put(BlockFace.WEST, BlockFace.SOUTH);
		toLeft.put(BlockFace.WEST_NORTH_WEST, BlockFace.SOUTH_WEST);
		toLeft.put(BlockFace.NORTH_WEST, BlockFace.SOUTH_WEST);
		toLeft.put(BlockFace.NORTH_NORTH_WEST, BlockFace.SOUTH_WEST);
	}

	/**
	 * Get the BlockFace 90 degrees to the left of the given BlockFace.
	 *
	 * @param facing
	 * @return
	 */
	public static BlockFace getLeft(BlockFace facing) {
		if (!toLeft.containsKey(facing)) {
			throw new IllegalArgumentException("can't pass " + facing + " to getLeft()");
		}
		return toLeft.get(facing);
	}

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
