package me.desht.dhutils.cuboid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import me.desht.dhutils.LogUtils;
import me.desht.dhutils.block.BlockType;
import me.desht.dhutils.block.BlockUtils;
import me.desht.dhutils.block.MaterialWithData;
import net.minecraft.server.v1_4_5.ChunkCoordIntPair;
import net.minecraft.server.v1_4_5.EntityPlayer;
import net.minecraft.server.v1_4_5.EnumSkyBlock;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.craftbukkit.v1_4_5.CraftChunk;
import org.bukkit.craftbukkit.v1_4_5.CraftWorld;
import org.bukkit.craftbukkit.v1_4_5.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

import com.google.common.base.Joiner;

public class Cuboid implements Iterable<Block>, Cloneable, ConfigurationSerializable {
	protected final String worldName;
	protected final int x1, y1, z1;
	protected final int x2, y2, z2;

	/**
	 * Construct a Cuboid given two Location objects which represent any two corners
	 * of the Cuboid.
	 * 
	 * @param l1 one of the corners
	 * @param l2 the other corner
	 */
	public Cuboid(Location l1, Location l2) {
		if (! l1.getWorld().equals(l2.getWorld())) {
			throw new IllegalArgumentException("locations must be on the same world");
		}
		worldName = l1.getWorld().getName();
		x1 = Math.min(l1.getBlockX(), l2.getBlockX());
		y1 = Math.min(l1.getBlockY(), l2.getBlockY());
		z1 = Math.min(l1.getBlockZ(), l2.getBlockZ());
		x2 = Math.max(l1.getBlockX(), l2.getBlockX());
		y2 = Math.max(l1.getBlockY(), l2.getBlockY());
		z2 = Math.max(l1.getBlockZ(), l2.getBlockZ());
	}

	/**
	 * Construct a one-block Cuboid at the given Location of the Cuboid.
	 * 
	 * @param l1 location of the Cuboid
	 */
	public Cuboid(Location l1) {
		this(l1, l1);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other the Cuboid to copy
	 */
	public Cuboid(Cuboid other) {
		this(other.getWorld().getName(), other.x1, other.y1, other.z1, other.x2, other.y2, other.z2);
	}

	/**
	 * Construct a Cuboid in the given World and xyz co-ordinates
	 * 
	 * @param world the Cuboid's world
	 * @param x1 X co-ordinate of corner 1
	 * @param y1 Y co-ordinate of corner 1
	 * @param z1 Z co-ordinate of corner 1
	 * @param x2 X co-ordinate of corner 2
	 * @param y2 Y co-ordinate of corner 2
	 * @param z2 Z co-ordinate of corner 2
	 */
	public Cuboid(World world, int x1, int y1, int z1, int x2, int y2, int z2) { 
		this.worldName = world.getName();
		this.x1 = Math.min(x1, x2);
		this.x2 = Math.max(x1, x2);
		this.y1 = Math.min(y1, y2);
		this.y2 = Math.max(y1, y2);
		this.z1 = Math.min(z1, z2);
		this.z2 = Math.max(z1, z2);
	}

	/**
	 * Construct a Cuboid in the given world name and xyz co-ordinates.
	 * 
	 * @param worldName the Cuboid's world name
	 * @param x1 X co-ordinate of corner 1
	 * @param y1 Y co-ordinate of corner 1
	 * @param z1 Z co-ordinate of corner 1
	 * @param x2 X co-ordinate of corner 2
	 * @param y2 Y co-ordinate of corner 2
	 * @param z2 Z co-ordinate of corner 2
	 */
	private Cuboid(String worldName, int x1, int y1, int z1, int x2, int y2, int z2) {
		this.worldName = worldName;
		this.x1 = Math.min(x1, x2);
		this.x2 = Math.max(x1, x2);
		this.y1 = Math.min(y1, y2);
		this.y2 = Math.max(y1, y2);
		this.z1 = Math.min(z1, z2);
		this.z2 = Math.max(z1, z2);
	}
	
	public Cuboid(Map<String,Object> map) {
		worldName = (String)map.get("worldName");
		x1 = (Integer) map.get("x1");
		x2 = (Integer) map.get("x2");
		y1 = (Integer) map.get("y1");
		y2 = (Integer) map.get("y2");
		z1 = (Integer) map.get("z1");
		z2 = (Integer) map.get("z2");
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("worldName", worldName);
		map.put("x1", x1);
		map.put("y1", y1);
		map.put("z1", z1);
		map.put("x2", x2);
		map.put("y2", y2);
		map.put("z2", z2);
		return map;
	}

	/**
	 * Get the Location of the lower northeast corner of the Cuboid (minimum XYZ
	 * co-ordinates).
	 * 
	 * @return Location of the lower northeast corner
	 */
	public Location getLowerNE() {
		return new Location(getWorld(), x1, y1, z1);
	}

	/**
	 * Get the Location of the upper southwest corner of the Cuboid (maximum XYZ
	 * co-ordinates).
	 * 
	 * @return Location of the upper southwest corner
	 */
	public Location getUpperSW() {
		return new Location(getWorld(), x2, y2, z2);
	}

	/**
	 * Get the the centre of the Cuboid
	 * 
	 * @return Location at the centre of the Cuboid
	 */
	public Location getCenter() {
		return new Location(getWorld(), getLowerX() + (getUpperX() - getLowerX()) / 2,
		                    getLowerY() + (getUpperY() - getLowerY()) / 2,
		                    getLowerZ() + (getUpperZ() - getLowerZ()) / 2);
	}

	/**
	 * Get the Cuboid's world.
	 *
	 * @return the World object representing this Cuboid's world
	 * @throws IllegalStateException if the world is not loaded
	 */
	public World getWorld() {
		World world = Bukkit.getWorld(worldName);
		if (world == null) {
			throw new IllegalStateException("world '" + worldName + "' is not loaded");
		}
		return world;
	}

	/**
	 * Get the size of this Cuboid along the X axis
	 * 
	 * @return	Size of Cuboid along the X axis
	 */
	public int getSizeX() {
		return (x2 - x1) + 1;
	}

	/**
	 * Get the size of this Cuboid along the Y axis
	 * 
	 * @return	Size of Cuboid along the Y axis
	 */
	public int getSizeY() {
		return (y2 - y1) + 1;
	}

	/**
	 * Get the size of this Cuboid along the Z axis
	 * 
	 * @return	Size of Cuboid along the Z axis
	 */
	public int getSizeZ() {
		return (z2 - z1) + 1;
	}

	/**
	 * Get the minimum X co-ordinate of this Cuboid
	 * 
	 * @return	the minimum X co-ordinate
	 */
	public int getLowerX() {
		return x1;
	}

	/**
	 * Get the minimum Y co-ordinate of this Cuboid
	 * 
	 * @return	the minimum Y co-ordinate
	 */
	public int getLowerY() {
		return y1;
	}

	/**
	 * Get the minimum Z co-ordinate of this Cuboid
	 * 
	 * @return	the minimum Z co-ordinate
	 */
	public int getLowerZ() {
		return z1;
	}

	/**
	 * Get the maximum X co-ordinate of this Cuboid
	 * 
	 * @return	the maximum X co-ordinate
	 */
	public int getUpperX() {
		return x2;
	}

	/**
	 * Get the maximum Y co-ordinate of this Cuboid
	 * 
	 * @return	the maximum Y co-ordinate
	 */
	public int getUpperY() {
		return y2;
	}

	/**
	 * Get the maximum Z co-ordinate of this Cuboid
	 * 
	 * @return	the maximum Z co-ordinate
	 */
	public int getUpperZ() {
		return z2;
	}

	/**
	 * Get the Blocks at the eight corners of the Cuboid.
	 *
	 * @return array of Block objects representing the Cuboid corners
	 */
	public Block[] corners() {
		Block[] res = new Block[8];
		World w = getWorld();
		res[0] = w.getBlockAt(x1, y1, z1);
		res[1] = w.getBlockAt(x1, y1, z2);
		res[2] = w.getBlockAt(x1, y2, z1);
		res[3] = w.getBlockAt(x1, y2, z2);
		res[4] = w.getBlockAt(x2, y1, z1);
		res[5] = w.getBlockAt(x2, y1, z2);
		res[6] = w.getBlockAt(x2, y2, z1);
		res[7] = w.getBlockAt(x2, y2, z2);
		return res;
	}

	/**
	 * Expand the Cuboid in the given direction by the given amount.  Negative amounts will
	 * shrink the Cuboid in the given direction.  Shrinking a cuboid's face past the opposite face
	 * is not an error and will return a valid Cuboid.
	 * 
	 * @param dir	the direction in which to expand
	 * @param amount	the number of blocks by which to expand
	 * @return	a new Cuboid expanded by the given direction and amount
	 */
	public Cuboid expand(CuboidDirection dir, int amount) {		
		switch (dir) {
		case North:
			return new Cuboid(worldName, x1 - amount, y1, z1, x2, y2, z2);
		case South:
			return new Cuboid(worldName, x1, y1, z1, x2 + amount, y2, z2);
		case East:
			return new Cuboid(worldName, x1, y1, z1 - amount, x2, y2, z2);
		case West:
			return new Cuboid(worldName, x1, y1, z1, x2, y2, z2 + amount);
		case Down:
			return new Cuboid(worldName, x1, y1 - amount, z1, x2, y2, z2);
		case Up:
			return new Cuboid(worldName, x1, y1, z1, x2, y2 + amount, z2);
		default:
			throw new IllegalArgumentException("invalid direction " + dir);
		}
	}

	/**
	 * Shift the Cuboid in the given direction by the given amount.
	 * 
	 * @param dir	the direction in which to shift
	 * @param amount	the number of blocks by which to shift
	 * @return	a new Cuboid shifted by the given direction and amount
	 */
	public Cuboid shift(CuboidDirection dir, int amount) {
		return expand(dir, amount).expand(dir.opposite(), -amount);
	}

	/**
	 * Outset (grow) the Cuboid in the given direction by the given amount.
	 * 
	 * @param dir	the direction in which to outset (must be Horizontal, Vertical, or Both)
	 * @param amount	the number of blocks by which to outset
	 * @return	a new Cuboid outset by the given direction and amount
	 */
	public Cuboid outset(CuboidDirection dir, int amount) {
		Cuboid c;
		switch (dir) {
		case Horizontal:
			c = expand(CuboidDirection.North, amount).expand(CuboidDirection.South, amount).expand(CuboidDirection.East, amount).expand(CuboidDirection.West, amount);
			break;
		case Vertical:
			c = expand(CuboidDirection.Down, amount).expand(CuboidDirection.Up, amount);
			break;
		case Both:
			c = outset(CuboidDirection.Horizontal, amount).outset(CuboidDirection.Vertical, amount);
			break;
		default:
			throw new IllegalArgumentException("invalid direction " + dir);
		}
		return c;
	}

	/**
	 * Inset (shrink) the Cuboid in the given direction by the given amount.  Equivalent
	 * to calling outset() with a negative amount.
	 * 
	 * @param dir the direction in which to inset (must be Horizontal, Vertical, or Both)
	 * @param amount	the number of blocks by which to inset
	 * @return	a new Cuboid inset by the given direction and amount
	 */
	public Cuboid inset(CuboidDirection dir, int amount) {
		return outset(dir, -amount);
	}

	/**
	 * Return true if the point at (x,y,z) is contained within this Cuboid.
	 * 
	 * @param x	the X co-ordinate
	 * @param y	the Y co-ordinate
	 * @param z	the Z co-ordinate
	 * @return	true if the given point is within this Cuboid, false otherwise
	 */
	public boolean contains(int x, int y, int z) {
		return x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2;
	}

	/**
	 * Check if the given Block is contained within this Cuboid.
	 * 
	 * @param b	the Block to check for
	 * @return	true if the Block is within this Cuboid, false otherwise
	 */
	public boolean contains(Block b) {
		return contains(b.getLocation());
	}

	/**
	 * Check if the given Location is contained within this Cuboid.
	 * 
	 * @param l	the Location to check for
	 * @return	true if the Location is within this Cuboid, false otherwise
	 */
	public boolean contains(Location l) {
		if (! worldName.equals(l.getWorld().getName())) {
			return false;
		}
		return contains(l.getBlockX(), l.getBlockY(), l.getBlockZ());
	}

	/**
	 * Get the volume of this Cuboid.
	 * 
	 * @return	the Cuboid volume, in blocks
	 */
	public int volume() {
		return getSizeX() * getSizeY() * getSizeZ();
	}

	/**
	 * Get the average light level of all empty (air) blocks in the Cuboid.  Returns 0 
	 * if there are no empty blocks.
	 * 
	 * @return	the average light level of this Cuboid
	 */
	public byte averageLightLevel() {
		long total = 0;
		int n = 0;
		for (Block b : this) {
			if (b.isEmpty()) {
				total += b.getLightLevel();
				++n;
			}
		}
		return n > 0 ? (byte) (total / n) : 0;
	}

	/**
	 * Contract the Cuboid, returning a Cuboid with any air around the edges removed, just
	 * large enough to include all non-air blocks.
	 * 
	 * @return a new Cuboid with no external air blocks
	 */
	public Cuboid contract() {
		return this.
				contract(CuboidDirection.Down).
				contract(CuboidDirection.South).
				contract(CuboidDirection.East).
				contract(CuboidDirection.Up).
				contract(CuboidDirection.North).
				contract(CuboidDirection.West);
	}
	
	/**
	 * Contract the Cuboid in the given direction, returning a new Cuboid which has no exterior empty space.
	 * E.g. a direction of Down will push the top face downwards as much as possible.
	 * 
	 * @param dir	the direction in which to contract
	 * @return	a new Cuboid contracted in the given direction
	 */
	public Cuboid contract(CuboidDirection dir) {
		Cuboid face = getFace(dir.opposite());
		switch (dir) {
		case Down:
			while (face.containsOnly(0) && face.getLowerY() > this.getLowerY()) {
				face = face.shift(CuboidDirection.Down, 1);
			}
			return new Cuboid(worldName, x1, y1, z1, x2, face.getUpperY(), z2);
		case Up:
			while (face.containsOnly(0) && face.getUpperY() < this.getUpperY()) {
				face = face.shift(CuboidDirection.Up, 1);
			}
			return new Cuboid(worldName, x1, face.getLowerY(), z1, x2, y2, z2);
		case North:
			while (face.containsOnly(0) && face.getLowerX() > this.getLowerX()) {
				face = face.shift(CuboidDirection.North, 1);
			}
			return new Cuboid(worldName, x1, y1, z1, face.getUpperX(), y2, z2);
		case South:
			while (face.containsOnly(0) && face.getUpperX() < this.getUpperX()) {
				face = face.shift(CuboidDirection.South, 1);
			}
			return new Cuboid(worldName, face.getLowerX(), y1, z1, x2, y2, z2);
		case East:
			while (face.containsOnly(0) && face.getLowerZ() > this.getLowerZ()) {
				face = face.shift(CuboidDirection.East, 1);
			}
			return new Cuboid(worldName, x1, y1, z1, x2, y2, face.getUpperZ());
		case West:
			while (face.containsOnly(0) && face.getUpperZ() < this.getUpperZ()) {
				face = face.shift(CuboidDirection.West, 1);
			}
			return new Cuboid(worldName, x1, y1, face.getLowerZ(), x2, y2, z2);
		default:
			throw new IllegalArgumentException("Invalid direction " + dir);
		}
	}
	
	/**
	 * Get the Cuboid representing the face of this Cuboid.  The resulting Cuboid will be
	 * one block thick in the axis perpendicular to the requested face.
	 * 
	 * @param dir	which face of the Cuboid to get 
	 * @return	the Cuboid representing this Cuboid's requested face
	 */
	public Cuboid getFace(CuboidDirection dir	) {
		switch (dir) {
		case Down:
			return new Cuboid(worldName, x1, y1, z1, x2, y1, z2);
		case Up:
			return new Cuboid(worldName, x1, y2, z1, x2, y2, z2);
		case North:
			return new Cuboid(worldName, x1, y1, z1, x1, y2, z2);
		case South:
			return new Cuboid(worldName, x2, y1, z1, x2, y2, z2);
		case East:
			return new Cuboid(worldName, x1, y1, z1, x2, y2, z1);
		case West:
			return new Cuboid(worldName, x1, y1, z2, x2, y2, z2);
		default:
			throw new IllegalArgumentException("Invalid direction " + dir);
		}
	}

	/**
	 * Check if the Cuboid contains only blocks of the given type
	 * 
	 * @param blockId	the block ID to check for
	 * @return			true if this Cuboid contains only blocks of the given type
	 */
	public boolean containsOnly(int blockId) {
		for (Block b : this) {
			if (b.getTypeId() != blockId) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Get the Cuboid big enough to hold both this Cuboid and the given one.
	 * 
	 * @param other
	 * @return	a new Cuboid large enough to hold this Cuboid and the given Cuboid
	 */
	public Cuboid getBoundingCuboid(Cuboid other) {
		if (other == null) {
			return this;
		}
		
		int xMin = Math.min(getLowerX(), other.getLowerX());
		int yMin = Math.min(getLowerY(), other.getLowerY());
		int zMin = Math.min(getLowerZ(), other.getLowerZ());
		int xMax = Math.max(getUpperX(), other.getUpperX());
		int yMax = Math.max(getUpperY(), other.getUpperY());
		int zMax = Math.max(getUpperZ(), other.getUpperZ());
		
		return new Cuboid(worldName, xMin, yMin, zMin, xMax, yMax, zMax);
	}
	
	/**
	 * Get a block relative to the lower NE point of the Cuboid.
	 * 
	 * @param x	the X co-ordinate
	 * @param y	the Y co-ordinate
	 * @param z	the Z co-ordinate
	 * @return	the block at the given position
	 */
	public Block getRelativeBlock(int x, int y, int z) {
		return getWorld().getBlockAt(x1 + x, y1 + y, z1 + z);
	}

	/**
	 * Get a block relative to the lower NE point of the Cuboid in the given World.  This 
	 * version of getRelativeBlock() should be used if being called many times, to avoid
	 * excessive calls to getWorld().
	 * 
	 * @param w	the World
	 * @param x	the X co-ordinate	
	 * @param y	the Y co-ordinate	
	 * @param z	the Z co-ordinate	
	 * @return	the block at the given position
	 */
	public Block getRelativeBlock(World w, int x, int y, int z) {
		return w.getBlockAt(x1 + x, y1 + y, z1 + z);
	}

	/**
	 * Get a list of the chunks which are fully or partially contained in this cuboid.
	 * 
	 * @return a list of Chunk objects
	 */
	public List<Chunk> getChunks() {
		List<Chunk> res = new ArrayList<Chunk>();
	
		World w = getWorld();
		int x1 = getLowerX() & ~0xf; int x2 = getUpperX() & ~0xf;
		int z1 = getLowerZ() & ~0xf; int z2 = getUpperZ() & ~0xf;
		for (int x = x1; x <= x2; x += 16) {
			for (int z = z1; z <= z2; z += 16) {
				res.add(w.getChunkAt(x >> 4, z >> 4));
			}
		}
		return res;
	}
	
	/**
	 * Set all the blocks within the Cuboid to the given block ID and data byte.
	 * 
	 * @param blockId
	 * @param data
	 * @param fast
	 */
	public void fill(int blockId, byte data) {
		long start = System.nanoTime();
	
		if (blockId == 0) {
			clear(false);
		} else {
			for (Block b : this) {
				b.setTypeIdAndData(blockId, data, false);
			}
		}
	
		LogUtils.finer("Cuboid: " + this + ": set " + blockId + "/" + data + ": " + (System.nanoTime() - start) + "ns");
	}

	/**
	 * Set all the blocks within the Cuboid to the given MaterialWithData
	 * 
	 * @param mat	The material to set
	 */
	public void fill(MaterialWithData mat) {
		fill(mat.getId(), mat.getData());
	}

	/**
	 * Set all the blocks within the Cuboid to the given block ID and data, using fast direct chunk access.
	 * This will require a call to sendClientChanges() later to ensure clients see the updates.
	 * 
	 * @param blockId	The block ID to set
	 * @param data 	The data byte to set
	 */
	public void fillFast(int blockId, byte data) {
		long start = System.nanoTime();
		
		if (blockId == 0) {
			clear(true);
		} else {
			for (Block b : this) {
				BlockUtils.setBlockFast(b, blockId, data);
			}
		}
		LogUtils.finer("Cuboid: " + this + ": set " + blockId + "/" + data + ": " + (System.nanoTime() - start) + "ns");
	}

	public void fillFast(MaterialWithData mat) {
		fillFast(mat.getId(), mat.getData());
	}

	/**
	 * Delete blocks, but don't allow items to drop (paintings are not
	 * blocks, and are not included).  Does not check for blocks attached to the
	 * outside faces of the Cuboid.
	 *
	 * @param fast	Use low-level NMS calls to clear the Cuboid to avoid excessive
	 * 			lighting recalculation
	 */
	public void clear(boolean fast) {
		// first remove blocks that might pop off & leave a drop
		for (Block b : this) {
			if (BlockType.shouldPlaceLast(b.getTypeId())) {
				if (fast) {
					BlockUtils.setBlockFast(b, 0);
				} else {
					b.setTypeId(0);
				}
			} else if (BlockType.isContainerBlock(b.getTypeId())) {
				// also check if this is a container, and empty it if necessary
				BlockState state = b.getState();
				if (state instanceof InventoryHolder) {
					InventoryHolder ih = (InventoryHolder) state;
					ih.getInventory().clear();
				}
			}
		}
		// now wipe all (remaining) blocks
		if (fast) {
			for (Block b : this) {
				BlockUtils.setBlockFast(b, 0);
			}
		} else {
			for (Block b : this) {
				b.setTypeId(0);
			}
		}
	}

	/**
	 * Force lighting to be recalculated for all chunks occupied by the cuboid.
	 */
	public void initLighting() {	
		for (Chunk c : getChunks()) {
			((CraftChunk)c).getHandle().initLighting();
			LogUtils.finer("Cuboid: initLighting: chunk " + c + ": relit"); 
		}
	}

	/**
	 * Set the light level of all blocks within this Cuboid.
	 * 
	 * @param level			the required light level
	 */
	public void forceLightLevel(int level) {
		long start = System.nanoTime();
		net.minecraft.server.v1_4_5.World w = ((CraftWorld) getWorld()).getHandle();
		for (int x = getLowerX(); x < getUpperX(); x++) {
			for (int z = getLowerZ(); z < getUpperZ(); z++) {
				for (int y = getLowerY(); y < getUpperY(); y++) {
					// this was w.a() in CB 1.2
					w.b(EnumSkyBlock.BLOCK, x, y, z, level);
				}
			}
		}
		LogUtils.finer("Cuboid: forceLightLevel: " + this + " (level " + level + ") in " + (System.nanoTime() - start) + " ns");
	}

	/**
	 * Any players within the threshold distance of the cuboid may need
	 * to be notified of any fast changes that happened, to avoid "phantom" blocks showing
	 * up on the client.  Add the chunk coordinates of affected chunks to those players'
	 * chunk queue.
	 */
	public void sendClientChanges() {
		int threshold = (Bukkit.getServer().getViewDistance() << 4) + 32;
		threshold = threshold * threshold;
	
		List<ChunkCoordIntPair> pairs = new ArrayList<ChunkCoordIntPair>();
		for (Chunk c : getChunks()) {
			pairs.add(new ChunkCoordIntPair(c.getX(), c.getZ()));
		}
		int centerX = getLowerX() + getSizeX() / 2;	
		int centerZ = getLowerZ() + getSizeZ() / 2;
		for (Player player : getWorld().getPlayers()) {
			int px = player.getLocation().getBlockX();
			int pz = player.getLocation().getBlockZ();
			if ((px - centerX) * (px - centerX) + (pz - centerZ) * (pz - centerZ) < threshold) {
				queueChunks(((CraftPlayer) player).getHandle(), pairs);
			}
		}
	}

	@SuppressWarnings("unchecked")	
	private void queueChunks(EntityPlayer ep, List<ChunkCoordIntPair> pairs) {
		if (LogUtils.getLogLevel() == Level.FINEST) {	// if statement to avoid unnecessary Joiner call overhead
			LogUtils.finest("queue chunk co-ordinate pairs for " + ep.name + ": " + Joiner.on(", ").join(pairs));
		}
		Set<ChunkCoordIntPair> queued = new HashSet<ChunkCoordIntPair>();
		for (Object o : ep.chunkCoordIntPairQueue) {
			queued.add((ChunkCoordIntPair) o);
		}
		for (ChunkCoordIntPair pair : pairs) {
			if (!queued.contains(pair)) {
				ep.chunkCoordIntPairQueue.add(pair);
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<Block> iterator() {
		return new CuboidIterator(getWorld(), x1, y1, z1, x2, y2, z2);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Cuboid clone() {
		return new Cuboid(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new String("Cuboid: " + worldName + "," + x1 + "," + y1 + "," + z1 + "=>" + x2 + "," + y2 + "," + z2);
	}

	public class CuboidIterator implements Iterator<Block> {
		private World w;
		private int baseX, baseY, baseZ;
		private int x, y, z;
		private int sizeX, sizeY, sizeZ;

		public CuboidIterator(World w, int x1, int y1, int z1, int x2, int y2, int z2) {
			this.w = w;
			baseX = x1;
			baseY = y1;
			baseZ = z1;
			sizeX = Math.abs(x2 - x1) + 1;
			sizeY = Math.abs(y2 - y1) + 1;
			sizeZ = Math.abs(z2 - z1) + 1;
			x = y = z = 0;
		}

		public boolean hasNext() {
			return x < sizeX && y < sizeY && z < sizeZ;
		}

		public Block next() {
			Block b = w.getBlockAt(baseX + x, baseY + y, baseZ + z);
			if (++x >= sizeX) {
				x = 0;
				if (++y >= sizeY) {
					y = 0;
					++z;
				}
			}
			return b;
		}

		public void remove() {
			// nop
		}
	}

	public enum CuboidDirection {

		North, East, South, West, Up, Down, Horizontal, Vertical, Both, Unknown;
		
		public CuboidDirection opposite() {
			switch(this) {
				case North:
					return South;
				case East:
					return West;
				case South:
					return North;
				case West:
					return East;
				case Horizontal:
					return Vertical;
				case Vertical:
					return Horizontal;
				case Up:
					return Down;
				case Down:
					return Up;
				case Both:
					return Both;
				default:
					return Unknown;
			}
		}
	}
	
}
