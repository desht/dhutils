package me.desht.dhutils;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class PersistableLocation implements ConfigurationSerializable {
	private Location location;
	
	public PersistableLocation(Location l) {
		location = l;
	}
	
	public PersistableLocation(Map<String,Object> map) {
		World w = Bukkit.getWorld((String) map.get("world"));
		if (w == null) {
			throw new IllegalArgumentException("No such world " + map.get("world"));
		}
		location = new Location(w, (Double)map.get("x"), (Double)map.get("y"), (Double)map.get("z"));
		location.setPitch((Float) map.get("pitch"));
		location.setYaw((Float) map.get("yaw"));
	}
	
	public Location getLocation() {
		return location;
	}

	public Map<String, Object> serialize() {
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("world", location.getWorld().getName());
		map.put("x", location.getX());
		map.put("y", location.getY());
		map.put("z", location.getZ());
		map.put("pitch", location.getPitch());
		map.put("yaw", location.getYaw());
		return map;
	}

}
