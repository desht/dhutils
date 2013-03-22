package me.desht.dhutils;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.block.BlockFace;

public class BlockFaceUtil {

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

	public static BlockFace getLeft(BlockFace facing) {
		if (!toLeft.containsKey(facing)) {
			throw new IllegalArgumentException("can't pass " + facing + " to getLeft()");
		}
		return toLeft.get(facing);
	}
}
