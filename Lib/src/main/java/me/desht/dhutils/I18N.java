package me.desht.dhutils;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class I18N {
	private static final String LOCALE_NAME = "==NAME";

	public static final String FALLBACK_LOCALE = "en_US";
	public static final String DEFAULT_LANG_DIR = "lang";

	private static I18N instance = null;

	private String defaultLocale = FALLBACK_LOCALE;
	private final Map<String, Configuration> catalogs = new HashMap<String, Configuration>();
	private final Map<String, Configuration> playerLocales = new HashMap<String, Configuration>();
	private final File languageDirectory;

	private I18N(Plugin plugin, String defaultLocale, String langDir) {
		this.defaultLocale = defaultLocale == null ? FALLBACK_LOCALE : defaultLocale;
		loadCatalog(defaultLocale);
		this.languageDirectory = new File(plugin.getDataFolder(), langDir == null ? DEFAULT_LANG_DIR : langDir);
	}

	@SuppressWarnings("CloneDoesntCallSuperClone")
	@Override
	public I18N clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	/**
	 * Initialise the I18N instance with default parameters.
	 *
	 * @param plugin the plugin instance
	 * @return the singleton I18N object
	 */
	public static synchronized I18N init(Plugin plugin) {
		return init(plugin, null, null);
	}

	/**
	 * Initialise the I18N instance with custom parameters.
	 *
	 * @param plugin the plugin instance
	 * @param defaultLocale the default locale (or null to use the fallback)
	 * @param langDir the language directory (or null to use the default)
	 * @return the singleton I18N object
	 */
	public static synchronized I18N init(Plugin plugin, String defaultLocale, String langDir) {
		instance = new I18N(plugin, defaultLocale, langDir);
		return instance;
	}

	/**
	 * Get the singleton instance for this class.
	 *
	 * @return the singleton I18N object
	 */
	public static synchronized I18N getInstance() {
		return instance;
	}

	/**
	 * Get the current locale for the given command sender.
	 *
	 * @param sender the command sender to check for
	 * @return the current locale for the command sender
	 */
	public String getLocale(CommandSender sender) {
		return playerLocales.containsKey(sender.getName()) ? playerLocales.get(sender.getName()).getString(LOCALE_NAME) : defaultLocale;
	}

	/**
	 * Get the default locale for players who have specified a language we don't have a catalog for.
	 *
	 * @return the default locale
	 */
	public String getDefaultLocale() {
		return defaultLocale;
	}

	/**
	 * Set the default locale for players who have specified a language we don't have a catalog for.
	 *
	 * @param defaultLocale the new default locale to use
	 * @throws DHUtilsException if the new default locale is unrecognised
	 */
	public void setDefaultLocale(String defaultLocale) {
		loadCatalog(defaultLocale);
		this.defaultLocale = defaultLocale;
	}

	/**
	 * Set the locale for the given command sender.
	 *
	 * @param sender the command sender
	 * @param locale the new locale
	 */
	public void setLocale(CommandSender sender, String locale) {
		Configuration c = getMessageCatalog(locale);
		if (c != null) {
			playerLocales.put(sender.getName(), c);
		}
	}

	/**
	 * Get a message translation in the current default locale for the given message key
	 *
	 * @param key the message key
	 * @param args list of message parameters for the given key
	 * @return the translated message
	 */
	public String get(String key, Object... args) {
		return get(null, key, args);
	}

	/**
	 * Get a message translation in the given player's current locale for the given message key
	 *
	 * @param sender the player to translate for
	 * @param key the message key
	 * @param args list of message parameters for the given key
	 * @return the translated message
	 */
	public String get(CommandSender sender, String key, Object... args) {
		Configuration c = getMessageCatalog(sender);
		try {
			return MessageFormat.format(c.getString(key), args);
		} catch (IllegalArgumentException e) {
			LogUtils.warning("Error fomatting message for " + c.getString(LOCALE_NAME) + "/" + key + ": " + e.getMessage());
			return c.getString(key);
		}
	}

	private Configuration getMessageCatalog(CommandSender sender) {
		return sender == null ? playerLocales.get(defaultLocale) : playerLocales.get(sender.getName());
	}

	private Configuration getMessageCatalog(String locale) {
		if (!catalogs.containsKey(locale)) {
			try {
				catalogs.put(locale, loadCatalog(locale));
			} catch (DHUtilsException e) {
				LogUtils.warning("can't load " + locale + ": " + e.getMessage());
			}
		}
		return catalogs.get(locale);
	}

	private Configuration loadCatalog(String wantedLocale) {
		File wanted = new File(languageDirectory, wantedLocale + ".yml");
		File located = locateMessageFile(wanted);
		if (located == null) {
			throw new DHUtilsException("Unknown locale '" + wantedLocale + "'");
		}
		YamlConfiguration conf = YamlConfiguration.loadConfiguration(located);

		String basename = located.getName().replaceAll("\\.yml$", "");
		conf.set(LOCALE_NAME, basename);

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

	private File locateMessageFile(File wanted) {
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
