package me.desht.dhutils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.Plugin;

public class ConfigurationManager {
	private final Plugin plugin;
	private String prefix;
	private ConfigurationListener listener;
	private final Map<String,Class<?>> forceTypes = new HashMap<String,Class<?>>();

	public ConfigurationManager(Plugin plugin) {
		this.plugin = plugin;
		prefix = null;
		listener = null;

		Configuration config = plugin.getConfig();
		config.options().copyDefaults(true);
		plugin.saveConfig();
	}

	public Plugin getPlugin() {
		return plugin;
	}

	public Configuration getConfig() {
		return plugin.getConfig();
	}

	public void setConfigurationListener(ConfigurationListener listener) {
		this.listener = listener;
	}
	
	public void forceType(String key, Class<?> c) {
		forceTypes.put(key, c);
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void forceType(String key, String className) {
		try {
			Class<?> c = Class.forName(className);
			forceTypes.put(key, c);
		} catch (ClassNotFoundException e) {
			throw new DHUtilsException("Unknown type " + className);
		}
	}

	public Class<?> getType(String key) {
		return forceTypes.containsKey(key) ? forceTypes.get(key) : plugin.getConfig().getDefaults().get(key).getClass();
	}

	public Object get(String key) {
		key = addPrefix(key);
		Configuration config = plugin.getConfig();

		if (!config.contains(key)) {
			throw new DHUtilsException("No such config item: " + key);
		}

		return config.get(key);
	}

	public void set(String key, String val) {
		key = addPrefix(key);

		Object current = get(key);

		if (listener != null) {
			listener.onConfigurationValidate(this, key, val);
		}
		
		setItem(key, val);

		if (listener != null) {
			listener.onConfigurationChanged(this, key, current, get(key));
		}

		plugin.saveConfig();
	}

	public <T> void set(String key, List<T> val) {
		key = addPrefix(key);

		Object current = get(key);

		if (listener != null) {
			listener.onConfigurationValidate(this, key, val);
		}

		setItem(key, val);

		if (listener != null) {
			listener.onConfigurationChanged(this, key, current, get(key));
		}

		plugin.saveConfig();
	}

	public String addPrefix(String key) {
		return prefix == null ? key : prefix + "."	 + key;
	}

	public String removePrefix(String k) {
		return k.replaceAll("^" + prefix + "\\.", "");
	}

	@SuppressWarnings("unchecked")
	private void setItem(String key, String val) {
		Configuration config = plugin.getConfig();

		Class<?> c = getType(key);

		if (List.class.isInstance(c)) {
			List<String>list = new ArrayList<String>(1);
			list.add(val);
			handleListValue(config, key, list);
		} else if (String.class.isInstance(c)) {
			// should be marginally quicker than going through the following method...
			config.set(key, val);
		}  else if (Enum.class.isInstance(c)) {
			// this really isn't very pretty, but as far as I can tell there's no way to
			// do this with a parameterised Enum type		
			@SuppressWarnings("rawtypes")
			Class<? extends Enum> cSub = c.asSubclass(Enum.class);
			try {
				config.set(key, Enum.valueOf(cSub, val.toUpperCase()));
			} catch (IllegalArgumentException e) {
				throw new DHUtilsException("'" + val + "' is not a valid value for '" + key + "'");
			}
		} else {
			// the class we're converting to must have a constructor taking a single String argument
			try {
				Constructor<?> ctor = c.getDeclaredConstructor(String.class);
				config.set(key, ctor.newInstance(val));
			} catch (NoSuchMethodException e) {
				throw new DHUtilsException("Cannot convert '" + val + "' into a " + c.getName());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				if (e.getCause() instanceof NumberFormatException) {
					throw new DHUtilsException("Invalid numeric value: " + val);
				} else {
					e.printStackTrace();
				}
			}
		}
	}

	private <T> void setItem(String key, List<T> list) {
		Configuration config = plugin.getConfig();
		if (config.getDefaults().get(key) == null) {
			throw new DHUtilsException("No such key '" + key + "'");
		}
		if (!(config.getDefaults().get(key) instanceof List<?>)) {
			throw new DHUtilsException("Key '" + key + "' does not accept a list of values");
		}
		handleListValue(config, key, list);
	}

	@SuppressWarnings("unchecked")
	private static <T> void handleListValue(Configuration config, String key, List<T> list) {
		HashSet<T> current = new HashSet<T>((List<T>)config.getList(key));
		
		if (list.get(0).equals("-")) {
			// remove specifed item from list
			list.remove(0);
			current.removeAll(list);
		} else if (list.get(0).equals("=")) {
			// replace list
			list.remove(0);
			current = new HashSet<T>(list);
		} else if (list.get(0).equals("+")) {
			// append to list
			list.remove(0);
			current.addAll(list);
		} else {
			// append to list
			current.addAll(list);
		}

		config.set(key, new ArrayList<T>(current));
	}
}
