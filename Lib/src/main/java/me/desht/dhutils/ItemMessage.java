package me.desht.dhutils;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;

/**
 * Minecraft displays the item tooltip to the player briefly when it's changed.  Let's (ab)use that to
 * send a "popup" message to the player via one of these tooltips.  This is nice for temporary messages
 * where you don't want to clutter up the chat window.
 */
public class ItemMessage {
	private static final int INTERVAL = 20;  // ticks
	private static final int DEFAULT_DURATION = 2; // seconds

	private static final String DEF_FORMAT_1 = "%s";
	private static final String DEF_FORMAT_2 = " %s ";

	private final Plugin plugin;
	private final Map<String, WeakReference<Player>> players = new HashMap<String, WeakReference<Player>>();
	private String[] formats = new String[] { DEF_FORMAT_1, DEF_FORMAT_2 };

	/**
	 * Create a new ItemMessage object for the given plugin and player.
	 *
	 * @param plugin the plugin instance
	 * @param players the player(s)
	 * @throws IllegalStateException if the ProtocolLib plugin is not available
	 */
	public ItemMessage(Plugin plugin, Player... players) {
		Plugin p = Bukkit.getPluginManager().getPlugin("ProtocolLib");
		if (p == null || !p.isEnabled()) {
			throw new IllegalStateException("ItemMessage can not be used without ProtocolLib");
		}
		this.plugin = plugin;
		for (Player player : players) {
			this.players.put(player.getName(), new WeakReference<Player>(player));
		}
	}

	/**
	 * Get the players for this ItemMessage object.  Note that any players who went offline
	 * since the object was created will not be included in the list.
	 *
	 * @return a list of the players in this object 
	 */
	public List<Player> getPlayers() {
		List<Player> result = new ArrayList<Player>();
		Iterator<WeakReference<Player>> iter = players.values().iterator();
		while (iter.hasNext()) {
			WeakReference<Player> ref = iter.next();
			if (ref.get() != null) {
				result.add(ref.get());
			} else {
				iter.remove();
			}
		}
		return result;
	}

	/**
	 * Add the given player(s) as message recipients.
	 *
	 * @param players the player(s) to add
	 */
	public void addPlayers(Player... players) {
		for (Player player : players) {
			this.players.put(player.getName(), new WeakReference<Player>(player));
		}
	}

	/**
	 * Set the alternating format strings for message display.  The strings must be different
	 * and must each contain one (and only one) occurrence of '%s'.
	 *
	 * @param f1 the first format string
	 * @param f2 the second format string
	 * @throws IllegalArgumentException if the strings are the same, or do not contain a %s
	 */
	public void setFormats(String f1, String f2) {
		Validate.isTrue(!f1.equals(f2), "format strings must be different");
		Validate.isTrue(f1.contains("%s"), "format string 1 must contain a %s");
		Validate.isTrue(f2.contains("%s"), "format string 2 must contain a %s");
		formats[0] = f1;
		formats[1] = f2;
	}

	private Player getPlayer(String playerName) {
		return players.get(playerName).get();
	}

	/**
	 * Check if the player for this ItemMessage object is available.
	 *
	 * @return true if the player is available, false otherwise
	 */
	public boolean isPlayerAvailable(String playerName) {
		return players.containsKey(playerName) && players.get(playerName).get() != null;
	}

	/**
	 * Send a popup message to the player, with a default duration of 2 seconds.
	 *
	 * @param message the message to send
	 * @throws IllegalStateException if the player is unavailable (e.g. went offline)
	 */
	public void sendMessage(String message) {
		sendMessage(message, DEFAULT_DURATION);
	}

	/**
	 * Send a popup message to the player, keeping it on-screen for the given duration.
	 *
	 * @param message the message to send
	 * @param duration the duration to keep the message on-screen for, in seconds
	 * @throws IllegalStateException if the player is unavailable (e.g. went offline)
	 */
	public void sendMessage(String message, int duration) {
		for (Player player : getPlayers()) {
			final int held = player.getInventory().getHeldItemSlot();
			new NamerTask(player.getName(), message, held, duration).runTaskTimer(plugin, 1L, INTERVAL);
		}
	}

	private class NamerTask extends BukkitRunnable implements Listener	{
		private final String playerName;
		private int slot;
		private int iterations;
		private final String message;

		public NamerTask(String playerName, String message, int slot, int duration) {
			this.playerName = playerName;
			this.iterations = Math.max(1, (duration * 20) / INTERVAL);
			this.slot = slot;
			this.message = message;
			Bukkit.getPluginManager().registerEvents(this, plugin);
		}

		@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
		public void onItemHeldChange(PlayerItemHeldEvent event) {
			Player player = event.getPlayer();
			if (isPlayerAvailable(player.getName())) {
				sendItemSlotChange(player, event.getPreviousSlot(), player.getInventory().getItem(event.getPreviousSlot()));
				slot = event.getNewSlot();
				refresh(event.getPlayer());
			}
		}

		@Override
		public void run() {
			if (isPlayerAvailable(playerName)) {
				if (iterations-- <= 0) {
					// finished - restore the previous item data and tidy up
					finish(getPlayer(playerName));
				} else {
					// refresh the item data
					refresh(getPlayer(playerName));
				}
			} else {
				// player probably disconnected
				cleanup();
			}
		}

		private void refresh(Player player) {
			sendItemSlotChange(player, slot, makeStack(player));
		}

		private void finish(Player player) {
			sendItemSlotChange(player, slot, player.getInventory().getItem(slot));
			cleanup();
		}

		private void cleanup() {
			cancel();
			HandlerList.unregisterAll(this);
		}

		private ItemStack makeStack(Player player) {
			ItemStack stack0 = player.getInventory().getItem(slot);
			ItemStack stack;
			if (stack0 == null || stack0.getType() == Material.AIR) {
				// an empty slot can't display any custom item name, so we need to fake an item
				// a snow layer is a good choice, since it's visually quite unobtrusive
				stack = new ItemStack(Material.SNOW, 1);
			} else {
				stack = new ItemStack(stack0.getType(), stack0.getAmount(), stack0.getDurability());
			}
			ItemMeta meta = Bukkit.getItemFactory().getItemMeta(stack.getType());
			// fool the client into thinking the item name has changed, so it actually (re)displays it
			meta.setDisplayName(String.format(formats[iterations % 2], message));
			stack.setItemMeta(meta);
			return stack;
		}

		private void sendItemSlotChange(Player player, int slot, ItemStack stack) {
			PacketContainer setSlot = new PacketContainer(103);
			// int field 0: window id (0 = player inventory)
			// int field 1: slot number (36 - 44 for player hotbar)
			setSlot.getIntegers().write(0, 0).write(1, slot + 36);
			setSlot.getItemModifier().write(0, stack);
			try {
				ProtocolLibrary.getProtocolManager().sendServerPacket(player, setSlot);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
}
