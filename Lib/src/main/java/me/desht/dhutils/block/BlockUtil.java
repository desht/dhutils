package me.desht.dhutils.block;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class BlockUtil {
	private static final BlockFace[] allFaces = new BlockFace[] {
		BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.DOWN, BlockFace.UP
	};

	public static BlockFace getNearestFace(Block block, Location loc) {
		Vector v = loc.toVector().subtract(block.getLocation().toVector());
		for (BlockFace face : allFaces) {
			Vector v1 = new Vector(face.getModX(), face.getModY(), face.getModZ());
			System.out.println("angle " + face + " = " + v1.angle(v));
		}
		return null;
	}
}
