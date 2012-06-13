package me.desht.dhutils.cuboid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class Cuboid implements Iterable<Block>, Cloneable {
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
	public Cuboid expand(Direction dir, int amount) {		
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
	public Cuboid shift(Direction dir, int amount) {
		return expand(dir, amount).expand(dir.opposite(), -amount);
	}

	/**
	 * Outset (grow) the Cuboid in the given direction by the given amount.
	 * 
	 * @param dir	the direction in which to outset (must be Horizontal, Vertical, or Both)
	 * @param amount	the number of blocks by which to outset
	 * @return	a new Cuboid outset by the given direction and amount
	 */
	public Cuboid outset(Direction dir, int amount) {
		Cuboid c;
		switch (dir) {
		case Horizontal:
			c = expand(Direction.North, amount).expand(Direction.South, amount).expand(Direction.East, amount).expand(Direction.West, amount);
			break;
		case Vertical:
			c = expand(Direction.Down, amount).expand(Direction.Up, amount);
			break;
		case Both:
			c = outset(Direction.Horizontal, amount).outset(Direction.Vertical, amount);
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
	public Cuboid inset(Direction dir, int amount) {
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
				contract(Direction.Down).
				contract(Direction.South).
				contract(Direction.East).
				contract(Direction.Up).
				contract(Direction.North).
				contract(Direction.West);
	}
	
	/**
	 * Contract the Cuboid in the given direction, returning a new Cuboid which has no exterior empty space.
	 * E.g. a direction of Down will push the top face downwards as much as possible.
	 * 
	 * @param dir	the direction in which to contract
	 * @return	a new Cuboid contracted in the given direction
	 */
	public Cuboid contract(Direction dir) {
		Cuboid face = getFace(dir.opposite());
		switch (dir) {
		case Down:
			while (face.containsOnly(0) && face.getLowerY() > this.getLowerY()) {
				face = face.shift(Direction.Down, 1);
			}
			return new Cuboid(worldName, x1, y1, z1, x2, face.getUpperY(), z2);
		case Up:
			while (face.containsOnly(0) && face.getUpperY() < this.getUpperY()) {
				face = face.shift(Direction.Up, 1);
			}
			return new Cuboid(worldName, x1, face.getLowerY(), z1, x2, y2, z2);
		case North:
			while (face.containsOnly(0) && face.getLowerX() > this.getLowerX()) {
				face = face.shift(Direction.North, 1);
			}
			return new Cuboid(worldName, x1, y1, z1, face.getUpperX(), y2, z2);
		case South:
			while (face.containsOnly(0) && face.getUpperX() < this.getUpperX()) {
				face = face.shift(Direction.South, 1);
			}
			return new Cuboid(worldName, face.getLowerX(), y1, z1, x2, y2, z2);
		case East:
			while (face.containsOnly(0) && face.getLowerZ() > this.getLowerZ()) {
				face = face.shift(Direction.East, 1);
			}
			return new Cuboid(worldName, x1, y1, z1, x2, y2, face.getUpperZ());
		case West:
			while (face.containsOnly(0) && face.getUpperZ() < this.getUpperZ()) {
				face = face.shift(Direction.West, 1);
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
	public Cuboid getFace(Direction dir	) {
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
	 * @param xthe X co-ordinate	
	 * @param ythe Y co-ordinate	
	 * @param zthe Z co-ordinate	
	 * @return
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

}
