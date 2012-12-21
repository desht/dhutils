package me.desht.dhutils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.server.v1_4_5.LocaleLanguage;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_4_5.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class I18N {
	private static final String FALLBACK_LOCALE = "en_US";
	
	private static String defaultLocale = FALLBACK_LOCALE;
	
	private final Plugin plugin;
	private final Map<String, Configuration> catalogs = new HashMap<String, Configuration>();
	private File languageDirectory;
	
	public I18N(Plugin plugin, String locale) {
		this.plugin = plugin;
		
		setLanguageDirectory("lang");
		loadCatalog(defaultLocale);
	}
	
	public I18N(Plugin plugin) {
		this(plugin, null);
	}

	public static String getLocale(CommandSender sender) {
		if (sender == null || !(sender instanceof Player))
			return defaultLocale;
			
		LocaleLanguage l = ((CraftPlayer) sender).getHandle().getLocale();
		try {
			Field f = LocaleLanguage.class.getDeclaredField("d");
			f.setAccessible(true);
			return f.get(l).toString();
		} catch (Exception e) {
			LogUtils.warning("Caught " + e.getClass() + " while trying to determine player's locale");
			return defaultLocale;
		}
	}
	
	public static String getDefaultLocale() {
		return defaultLocale;
	}

	public static void setDefaultLocale(String defaultLocale) {
		I18N.defaultLocale = defaultLocale;
	}

	public void setLanguageDirectory(String dirName) {
		languageDirectory = new File(plugin.getDataFolder(), dirName);
	}
	
	public File getLanguageDirectory() {
		return languageDirectory;
	}
	
	public String get(String key, Object... args) {
		return get(null, key, args);
	}
	
	public String get(Player p, String key, Object... args) {
		String locale = getLocale(p);
		String message = getMessageCatalog(locale).getString(key);
		
		try {
			return MessageFormat.format(message, args);
		} catch (IllegalArgumentException e) {
			LogUtils.warning("Error fomatting message for " + locale + "/" + key + ": " + e.getMessage());
			return message;
		}
	}
	
	private Configuration getMessageCatalog(String locale) {
		if (!catalogs.containsKey(locale)) {
			try {
				loadCatalog(locale);
			} catch (DHUtilsException e) {
				LogUtils.warning("can't load " + locale + ": " + e.getMessage());
			}
		}
		return catalogs.get(locale);
	}

	private Configuration loadCatalog(String wantedLocale) {
		File wanted = new File(getLanguageDirectory(), wantedLocale + ".yml");
		File located = locateMessageFile(wanted);
		if (located == null) {
			throw new DHUtilsException("Unknown locale '" + wantedLocale + "'");
		}
		YamlConfiguration conf = YamlConfiguration.loadConfiguration(located);
	
		Configuration fallbackMessages = catalogs.get(FALLBACK_LOCALE);
		
		// ensure that the config we're loading has all of the messages that the fallback has,
		// and make a note of any missing translations
		if (fallbackMessages != null && conf.getKeys(true).size() != fallbackMessages.getKeys(true).size()) {
			List<String> missingKeys = new ArrayList<String>();
			for (String key : fallbackMessages.getKeys(true)) {
				if (!conf.contains(key) && !fallbackMessages.isConfigurationSection(key)) {
					conf.set(key, fallbackMessages.get(key));
					missingKeys.add(key);
				}
			}
			conf.set("NEEDS_TRANSLATION", missingKeys);
			try {
				conf.save(located);
			} catch (IOException e) {
				LogUtils.warning("Can't write " + located + ": " + e.getMessage());
			}
		}
	
		return conf;
	}

	private static File locateMessageFile(File wanted) {
		if (wanted == null) {
			return null;
		}
		if (wanted.isFile() && wanted.canRead()) {
			return wanted;
		} else {
			String basename = wanted.getName().replaceAll("\\.yml$", "");
			if (basename.contains("_")) {
				basename = basename.replaceAll("_.+$", "");
			}
			File actual = new File(wanted.getParent(), basename + ".yml");
			if (actual.isFile() && actual.canRead()) {
				return actual;
			} else {
				return null;
			}
		}
	}
}
