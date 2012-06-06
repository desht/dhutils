package me.desht.dhutils;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class PersistableLocation implements ConfigurationSerializable {
	private final String worldName;
	private final double x, y, z;
	private final float pitch, yaw;
	private boolean savePitchAndYaw;

	public PersistableLocation(Location loc) {
		worldName = loc.getWorld().getName();
		x = loc.getX();
		y = loc.getY();
		z = loc.getZ();
		pitch = loc.getPitch();
		yaw = loc.getYaw();
		savePitchAndYaw = true;
	}

	public PersistableLocation(Map<String,Object> map) {
		worldName = (String)map.get("world");
		x = (Double)map.get("x");
		y = (Double)map.get("y");
		z = (Double)map.get("z");
		pitch = map.containsKey("pitch") ? ((Double) map.get("pitch")).floatValue() : 0.0f;
		yaw = map.containsKey("yaw") ? ((Double) map.get("yaw")).floatValue() : 0.0f;
		savePitchAndYaw = map.containsKey("pitch");
	}

	public String getWorldName() {
		return worldName;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}

	public boolean isSavePitchAndYaw() {
		return savePitchAndYaw;
	}

	public void setSavePitchAndYaw(boolean savePitchAndYaw) {
		this.savePitchAndYaw = savePitchAndYaw;
	}

	public Location getLocation() {
		World w = Bukkit.getWorld(worldName);
		if (w == null) {
			throw new IllegalStateException("World not loaded");
		}

		Location loc = new Location(w, x, y, z);
		loc.setPitch(pitch);
		loc.setYaw(yaw);
		return loc;
	}

	public Map<String, Object> serialize() {
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("world", worldName);
		map.put("x", x);
		map.put("y", y);
		map.put("z", z);
		if (savePitchAndYaw) {
			map.put("pitch", pitch);
			map.put("yaw", yaw);
		}
		return map;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(pitch);
		result = prime * result + ((worldName == null) ? 0 : worldName.hashCode());
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + Float.floatToIntBits(yaw);
		temp = Double.doubleToLongBits(z);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PersistableLocation other = (PersistableLocation) obj;
		if (Float.floatToIntBits(pitch) != Float.floatToIntBits(other.pitch))
			return false;
		if (worldName == null) {
			if (other.worldName != null)
				return false;
		} else if (!worldName.equals(other.worldName))
			return false;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		if (Float.floatToIntBits(yaw) != Float.floatToIntBits(other.yaw))
			return false;
		if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PersistableLocation [worldName=" + worldName + ", x=" + x + ", y=" + y + ", z=" + z + ", pitch="
				+ pitch + ", yaw=" + yaw + "]";
	}
}
