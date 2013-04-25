package me.desht.dhutils.block;

import java.util.HashMap;
import java.util.Map;

import me.desht.dhutils.FactoryMethod;
import me.desht.dhutils.LogUtils;
import me.desht.dhutils.cuboid.Cuboid;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Joiner;

public class MaterialWithData implements Cloneable {

	private final static Map<String, MaterialWithData> materialCache = new HashMap<String, MaterialWithData>();

	final int matId;
	final short data;
	final String[] metadata; // e.g. sign text

	private MaterialWithData(int matId, short data) {
		this.matId = matId;
		this.data = data;
		this.metadata = null;
	}

	private MaterialWithData(int mat) {
		this(mat, (byte) 0);
	}

	private MaterialWithData(MaterialWithData m) {
		this.matId = m.matId;
		this.data = m.data;
		this.metadata = m.metadata;
	}

	private MaterialWithData(String string) {
		String[] matAndText = string.split("=");
		String[] matAndData = matAndText[0].split(":");

		LogUtils.finest("MaterialWithData constructor: " + string);
		metadata = matAndText.length > 1 ? makeText(matAndText[1]) : null;

		if (matAndData[0].matches("^[0-9]+$")) {
			matId = Integer.parseInt(matAndData[0]);
		} else {
			// we'll look for the material string first in the WorldEdit BlockType class
			// and if that fails, we'll check for a Bukkit Material,
			// and if that fails, just throw an IllegalArgumentException
			BlockType b = BlockType.lookup(matAndData[0], true);
			if (b == null) {
				Material m = Material.matchMaterial(matAndData[0]);
				if (m == null) {
					throw new IllegalArgumentException("unknown material: " + matAndData[0]);
				}
				matId = m.getId();
			} else {
				matId = b.getID();
			}
		}
		if (matAndData.length < 2) {
			data = 0;
		} else {
			if (matAndData[1].matches("^[0-9]+$")) {
				data = Short.parseShort(matAndData[1]);
			} else if (matId == Material.WOOL.getId()) {
				// First look for the dye color string in the WorldEdit ClothColor class
				// and if that fails, check for a Bukkit DyeColor
				// and if that fails, just throw an IllegalArgumentException
				ClothColor cc = ClothColor.lookup(matAndData[1]);
				if (cc == null) {
					DyeColor dc = DyeColor.valueOf(matAndData[1].toUpperCase());
					if (dc == null) {
						throw new IllegalArgumentException("unknown dye colour: " + matAndData[1]);
					}
					data = (byte) dc.getWoolData();
				} else {
					data = (byte) cc.getID();
				}
			} else {
				throw new IllegalArgumentException("invalid data specification: " + matAndData[1]);
			}
		}
	}

	/**
	 * Get a MaterialData object from a String specification. The specification is a string or numeric Material name,
	 * optionally followed by a colon (:) and a numeric data byte.
	 * 
	 * @param spec
	 *            The specification
	 * @return The MaterialWithData object
	 * @throws IllegalArgumentException
	 *             if the specification is invalid
	 */
	@FactoryMethod
	public static MaterialWithData get(String spec) {
		spec = spec.toLowerCase();
		if (!materialCache.containsKey(spec)) {
			MaterialWithData mat = new MaterialWithData(spec);
			materialCache.put(spec, mat);
		}
		return materialCache.get(spec);
	}

	/**
	 * Get a MaterialData from a numeric ID and data byte, and extra material-dependent metadata (e.g. the text on a
	 * sign).
	 * 
	 * @param id
	 *            material ID
	 * @param data
	 *            material data byte
	 * @param metadata
	 *            list of Strings representing extra data for this object
	 * @return The MaterialWithData object
	 */
	@Deprecated
	public static MaterialWithData get(int id, byte data, String[] metadata) {
		return get(id, (short)data, metadata);
	}
	
	public static MaterialWithData get(int id, short data, String[] metadata) {
		String key = metadata == null ? String.format("%d:%d", id, data) : String.format("%d:%d=%s", id, data, Joiner.on(";").join(metadata));
		return get(key);
	}

	/**
	 * Get a MaterialData from a numeric ID and data byte.
	 * 
	 * @param id
	 *            the material ID
	 * @param data
	 *            the material data byte
	 * @return The MaterialWithData object
	 */
	@Deprecated
	public static MaterialWithData get(int id, byte data) {
		return get(String.format("%d:%d", id, data));
	}
	
	public static MaterialWithData get(int id, short data) {
		return get(String.format("%d:%d", id, data));
	}
	

	public static MaterialWithData get(Block b) {
		return get(String.format("%d:%d", b.getTypeId(), (short)b.getData()));
	}
	
	/**
	 * Get a MaterialData from a numeric ID. The data byte will be 0.
	 * 
	 * @param id
	 *            the material ID
	 * @return The MaterialWithData object
	 */
	public static MaterialWithData get(int id) {
		return get(String.format("%d:%d", id, 0));
	}

	/**
	 * Get the data byte for this MaterialWithData object
	 * 
	 * @return the material data byte
	 */
	public short getData() {
		return data;
	}

	/**
	 * Get the material ID for this MaterialWithData object
	 * 
	 * @return the material ID
	 */
	public int getId() {
		return matId;
	}

	/**
	 * Get the extra data for this MaterialWithData object
	 * 
	 * @return list of Strings representing extra data for this object
	 */
	public String[] getText() {
		return metadata;
	}

	/**
	 * Get the Bukkit Material for this MaterialWithData object
	 * 
	 * @return the Bukkit Material object
	 */
	public Material getBukkitMaterial() {
		return Material.getMaterial(matId);
	}

	/**
	 * Return an item stack of this material.
	 * 
	 * @return
	 */
	public ItemStack makeItemStack() {
		return makeItemStack(1);
	}

	public ItemStack makeItemStack(int amount) {
		return makeItemStack(amount, (short)getData());
	}

	public ItemStack makeItemStack(int amount, short damage) {
		return new ItemStack(getId(), amount, damage);
	}

	/**
	 * Get a rotated version of this MaterialData by altering the data byte appropriately.
	 * 
	 * @param rotation
	 *            The rotation in degrees; must be one of 90, 180 or 270 (any other value will return the original
	 *            material unchanged)
	 * @return a new MaterialWithData object, rotated as necessary
	 */
	public MaterialWithData rotate(int rotation) {
		short newData = data;
		switch (rotation) {
		case 270:
			newData = (byte) BlockData.rotate90Reverse(matId, data);
			break;
		case 180:
			newData = (byte) BlockData.rotate90(matId, data);
			newData = (byte) BlockData.rotate90(matId, newData);
			break;
		case 90:
			newData = (byte) BlockData.rotate90(matId, data);
			break;
		}
		return MaterialWithData.get(matId, newData, metadata);
	}

	/**
	 * Apply this MaterialWithData to the given block.
	 * 
	 * @param b
	 *            The block to apply the material to
	 */
	public void applyToBlock(Block b) {
		b.setTypeIdAndData(matId, (byte)data, false);
		if (metadata != null && (matId == 63 || matId == 68)) {
			// updating a wall sign or floor sign, with text
			Sign sign = (Sign) b.getState().getData();
			for (int i = 0; i < 4; i++) {
				sign.setLine(i, metadata[i]);
			}
			sign.update();
		}
	}

	/**
	 * Apply this MaterialWithData to the given block.
	 * 
	 * @param b
	 *            The block to apply the material to
	 */
	public void applyToBlock(Block b, MassBlockUpdate mbu) {
		mbu.setBlock(b.getX(), b.getY(), b.getZ(), matId, data);
		if (metadata != null && (matId == 63 || matId == 68)) {
			// updating a wall sign or floor sign, with text
			Sign sign = (Sign) b.getState().getData();
			for (int i = 0; i < 4; i++) {
				sign.setLine(i, metadata[i]);
			}
			sign.update();
		}
	}

	/**
	 * Apply this MaterialWithData to all the blocks within the given Cuboid using fast MBU calls.
	 * 
	 * @param c the Cuboid to fill
	 * @param mbu the MassBlockUpdate object to control fast block updating
	 */
	public void applyToCuboid(Cuboid c, MassBlockUpdate mbu) {
		c.fill(matId, (byte)data, mbu);
		mbu.notifyClients();
	}

	/**
	 * Apply this MaterialWithData to all the blocks within the given Cuboid.
	 * 
	 * @param c the Cuboid to fill
	 */
	public void applyToCuboid(Cuboid c) {
		c.fill(matId, (byte)data);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		Material mat = Material.getMaterial(matId);
		String matName = mat == null ? Integer.toString(matId) : mat.toString();
		StringBuilder s = new StringBuilder(matName);
		if (matId == Material.WOOL.getId()) {
			s.append(":").append(DyeColor.getByWoolData((byte)data).toString());
		} else {
			s.append(":").append(Short.toString(data));
		}
		return s.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public MaterialWithData clone() {
		return new MaterialWithData(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + data;
		result = prime * result + matId;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MaterialWithData other = (MaterialWithData) obj;
		if (matId != other.matId) {
			return false;
		} else if (data != other.data) {
			return false;
		} else {
			return true;
		}
	}

	private String[] makeText(String input) {
		String[] t = new String[] { "", "", "", "" };
		String[] s = input.split(";");
		for (int i = 0; i < 4 && i < s.length; i++) {
			t[i] = s[i];
		}
		return t;
	}

}
