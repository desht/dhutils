package me.desht.dhutils;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.bukkit.configuration.file.YamlConfiguration;

public class AttributeCollection extends ConfigurationManager {
	public AttributeCollection() {
		super(new YamlConfiguration());
	}
	
	public AttributeCollection(ConfigurationListener listener) {
		super(new YamlConfiguration());
		setConfigurationListener(listener);
	}
	
	public void registerAttribute(String attrName, Object def) {
		getConfig().addDefault(attrName, def);
	}
	
	public boolean contains(String key) {
		return getConfig().contains(key);
	}
	

	public Set<String> listAttributeKeys(boolean isSorted) {
		if (isSorted) {
			SortedSet<String> sorted = new TreeSet<String>(getConfig().getDefaults().getKeys(false));
			return sorted;
		} else {
			return getConfig().getDefaults().getKeys(false);
		}
	}
	
	public boolean hasAttribute(String k) {
		return getConfig().getDefaults().contains(k);
	}
}
