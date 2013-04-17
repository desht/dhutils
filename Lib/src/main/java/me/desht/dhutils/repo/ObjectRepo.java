package me.desht.dhutils.repo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import me.desht.dhutils.DHUtilsException;

public class ObjectRepo {
	private Map<String, DHStorable> repo = new HashMap<String, DHStorable>();

	public ObjectRepo() {

	}

	public void store(DHStorable object) throws IOException {
		String key = makeKey(object);

		if (repo.containsKey(key)) {
			throw new DHUtilsException("Already storing " + key);
		}

		repo.put(key, object);

		persist(object);
	}

	public <T extends DHStorable> T get(Class<T> clazz, String name) {
		String key = makeKey(clazz, name);
		if (!repo.containsKey(key)) {
			throw new DHUtilsException("No such object: " + key);
		}
		return clazz.cast(repo.get(key));
	}

	public void remove(DHStorable object) {
		String key = makeKey(object);
		if (!repo.containsKey(key)) {
			throw new DHUtilsException("No such object: " + key);
		}
		repo.remove(key);
		unpersist(object);
	}

	public boolean contains(DHStorable object) {
		String key = makeKey(object);
		return repo.containsKey(key);
	}

	private void persist(DHStorable object) throws IOException {
		if (object.getStorageFolder() == null) {
			// object doesn't want to be saved
			return;
		}
		File dest = new File(object.getStorageFolder(), object.getName() + ".yml");
		YamlConfiguration c = new YamlConfiguration();
		if (object instanceof ConfigurationSerializable) {
			c.set(object.getName(), object);
		} else {
			YamlConfiguration conf = new YamlConfiguration();
			expandMapIntoConfig(conf, object.freeze());
		}
		c.save(dest);
	}

	private void unpersist(DHStorable object) {
		if (object.getStorageFolder() == null) {
			// object doesn't want to be saved
			return;
		}
		File dest = new File(object.getStorageFolder(), object.getName() + ".yml");
		dest.delete();
	}

	private String makeKey(DHStorable object) {
		String k = object.getClass().getCanonicalName() + ":" + object.getName();
		return k;
	}

	private String makeKey(Class<? extends DHStorable> clazz, String name) {
		String k = clazz.getCanonicalName() + ":" + name;
		return k;
	}

	@SuppressWarnings("unchecked")
	private static void expandMapIntoConfig(ConfigurationSection conf, Map<String, Object> map) {
		for (Entry<String, Object> e : map.entrySet()) {
			if (e.getValue() instanceof Map<?,?>) {
				ConfigurationSection section = conf.createSection(e.getKey());
				Map<String,Object> subMap = (Map<String, Object>) e.getValue();
				expandMapIntoConfig(section, subMap);
			} else {
				conf.set(e.getKey(), e.getValue());
			}
		}
	}
}
