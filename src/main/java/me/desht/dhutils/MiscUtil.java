package me.desht.dhutils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagString;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class MiscUtil {
	private static Map<String, ChatColor> prevColours = new HashMap<String, ChatColor>();

	protected static final Logger logger = Logger.getLogger("Minecraft");
	protected static String messageFormat = "[?]: %s";

	public static void init(Plugin plugin) {
		messageFormat = "[" + plugin.getDescription().getName() + "]: %s";
	}

	public static void errorMessage(CommandSender sender, String string) {
		setPrevColour(sender.getName(), ChatColor.RED);
		message(sender, string, ChatColor.RED, Level.WARNING);
	}

	public static void statusMessage(CommandSender sender, String string) {
		setPrevColour(sender.getName(), ChatColor.AQUA);
		message(sender, string, ChatColor.AQUA, Level.INFO);
	}

	public static void alertMessage(CommandSender sender, String string) {
		setPrevColour(sender.getName(), ChatColor.YELLOW);
		message(sender, string, ChatColor.YELLOW, Level.INFO);
	}

	public static void generalMessage(CommandSender sender, String string) {
		setPrevColour(sender.getName(), ChatColor.WHITE);
		message(sender, string, Level.INFO);
	}

	public static void broadcastMessage(String string) {
		CommandSender sender = Bukkit.getConsoleSender();
		setPrevColour(sender.getName(), ChatColor.YELLOW);
		Bukkit.getServer().broadcastMessage(parseColourSpec(sender, "&4::&-" + string)); //$NON-NLS-1$
	}

	private static void setPrevColour(String name, ChatColor colour) {
		prevColours.put(name, colour);
	}

	private static ChatColor getPrevColour(String name) {
		if (!prevColours.containsKey(name)) {
			setPrevColour(name, ChatColor.WHITE);
		}
		return prevColours.get(name);
	}

	public static void rawMessage(CommandSender sender, String string) {
		for (String line : string.split("\\n")) { //$NON-NLS-1$
			if (sender instanceof Player) {
				sender.sendMessage(line);
			} else {
				LogUtils.info(ChatColor.stripColor(parseColourSpec(sender, line)));
			}
		}
	}

	private static void message(CommandSender sender, String string, Level level) {
		for (String line : string.split("\\n")) { //$NON-NLS-1$
			if (sender instanceof Player) {
				sender.sendMessage(parseColourSpec(sender, line));
			} else {
				LogUtils.log(level, ChatColor.stripColor(parseColourSpec(sender, line)));
			}
		}
	}

	private static void message(CommandSender sender, String string, ChatColor colour, Level level) {
		for (String line : string.split("\\n")) { //$NON-NLS-1$
			if (sender instanceof Player) {
				sender.sendMessage(colour + parseColourSpec(sender, line));
			} else {
				LogUtils.log(level, ChatColor.stripColor(parseColourSpec(sender, line)));
			}
		}
	}

	public static String formatLocation(Location loc) {
		return String.format("%d,%d,%d,%s", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName());
	}

	public static Location parseLocation(String arglist) {
		return parseLocation(arglist, null);
	}

	public static Location parseLocation(String arglist, CommandSender sender) {
		String s = sender instanceof Player ? "" : ",worldname";
		String args[] = arglist.split(",");

		try {
			int x = Integer.parseInt(args[0]);
			int y = Integer.parseInt(args[1]);
			int z = Integer.parseInt(args[2]);
			World w = (sender instanceof Player) ? findWorld(args[3]) : ((Player)sender).getWorld();
			return new Location(w, x, y, z);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException("You must specify all of x,y,z" + s + ".");
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid number in " + arglist);
		}
	}

	private static final Pattern colourPat = Pattern.compile("(?<!&)&(?=[0-9a-fA-Fk-oK-OrR])");

	public static String parseColourSpec(String spec) {
		return parseColourSpec(null, spec);
	}

	public static String parseColourSpec(CommandSender sender, String spec) {
		String who = sender == null ? "*" : sender.getName();
		String res = colourPat.matcher(spec).replaceAll("\u00A7");
		return res.replace("&-", getPrevColour(who).toString()).replace("&&", "&");
	}

	public static String unParseColourSpec(String spec) {
		return spec.replaceAll("\u00A7", "&");
	}

	public static World findWorld(String worldName) {
		World w = Bukkit.getServer().getWorld(worldName);
		if (w != null) {
			return w;
		} else {
			throw new IllegalArgumentException("World " + worldName + " was not found on the server.");
		}
	}

	public static List<String> splitQuotedString(String s) {
		List<String> matchList = new ArrayList<String>();

		Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
		Matcher regexMatcher = regex.matcher(s);

		while (regexMatcher.find()) {
			if (regexMatcher.group(1) != null) {
				// Add double-quoted string without the quotes
				matchList.add(regexMatcher.group(1));
			} else if (regexMatcher.group(2) != null) {
				// Add single-quoted string without the quotes
				matchList.add(regexMatcher.group(2));
			} else {
				// Add unquoted word
				matchList.add(regexMatcher.group());
			}
		}

		return matchList;
	}

	public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
		List<T> list = new ArrayList<T>(c);
		java.util.Collections.sort(list);
		return list;
	}

	public static void playNamedSound(Location loc, String sound, float volume, float pitch) {
		if (sound.isEmpty())
			return;
		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();
		((CraftWorld)loc.getWorld()).getHandle().makeSound(x, y, z, sound, volume, pitch);
	}

	/**
	 * Set the item name and lore for an item using the MC 1.4 item renaming system.  Hopefully this is 
	 * just temporary until Bukkit adds an API for it.  Credit to nisovin for this code.
	 * 
	 * @param item	The item to be renamed
	 * @param name	New name for the item
	 * @param lore	Item lore (can be null)
	 * @return	the renamed item
	 */
	public static ItemStack setItemNameAndLore(ItemStack item, String name, String[] lore) {
		CraftItemStack craftItem;        
		if (item instanceof CraftItemStack) {
			craftItem = (CraftItemStack)item;
		} else {
			craftItem = new CraftItemStack(item);
		}

		NBTTagCompound tag = craftItem.getHandle().tag;
		if (tag == null) {
			tag = new NBTTagCompound();
			craftItem.getHandle().tag = tag;
		}
		NBTTagCompound disp = tag.getCompound("display");
		if (disp == null) {
			disp = new NBTTagCompound("display");
		}

		disp.setString("Name", name);

		if (lore != null && lore.length > 0) {
			NBTTagList list = new NBTTagList("Lore");
			disp.set("Lore", list);
			for (String l : lore) {
				list.add(new NBTTagString("", l));
			}
		}

		tag.setCompound("display", disp);

		return craftItem;
	}

	public static void removeItemName(ItemStack item) {
		CraftItemStack craftItem;        
		if (item instanceof CraftItemStack) {
			craftItem = (CraftItemStack)item;
		} else {
			craftItem = new CraftItemStack(item);
		}
		craftItem.getHandle().tag = null;
	}
}
