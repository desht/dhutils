package me.desht.dhutils.nms;

import java.lang.reflect.InvocationTargetException;

import me.desht.dhutils.nms.api.NMSAbstraction;

import org.bukkit.plugin.Plugin;

public class NMSHelper {
	private static NMSAbstraction nms = null;
	
	public static NMSAbstraction init(Plugin plugin) throws ClassNotFoundException, IllegalArgumentException,
			SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		
		String serverPackageName = plugin.getServer().getClass().getPackage().getName();
		String pluginPackageName = plugin.getClass().getPackage().getName();
		
		// Get full package string of CraftServer.
		// org.bukkit.craftbukkit.vX_Y_Z (or for pre-refactor, just org.bukkit.craftbukkit)
		String version = serverPackageName.substring(serverPackageName.lastIndexOf('.') + 1);
		if (version.equals("craftbukkit")) {
			// No numeric (versioned) package component found - must be pre-refactoring
			version = "pre";
		}

		// NOTE: this assumes that dhutils is shaded into the plugin as <plugin-main-package>.dhutils
		final Class<?> clazz = Class.forName(pluginPackageName + ".dhutils.nms." + version + ".NMSHandler");

		// Check if we have a NMSAbsraction implementing class at that location.
		NMSAbstraction nmsAbstraction = null;
		if (NMSAbstraction.class.isAssignableFrom(clazz)) { // Make sure it actually implements NMSAbstraction
			nmsAbstraction = (NMSAbstraction) clazz.getConstructor().newInstance();
		}

		return nmsAbstraction;
	}
	
	public static NMSAbstraction getNMS(Plugin plugin) throws IllegalArgumentException, SecurityException, ClassNotFoundException,
			InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (nms == null) {
			nms = init(plugin);
		}
		return nms;
	}
}
