package me.desht.dhutils;

import java.util.Map;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.MapMaker;

// Not thread safe

/**
 * Class which allows multiple plugins to co-operatively permit or deny flight for players.
 *
 * With thanks to Comphenix:
 * https://forums.bukkit.org/threads/proposal-central-flight-mediation-plugin.214379/
 */
public class FlightController {
	public interface OnControllerChanged {
		/**
		 * Invoked when a new plugin has taken control of the flight value of a player.
		 * @param player - the player.
		 * @param controller - the new controller plugin.
		 */
		public void onChanged(Player player, Plugin controller);
	}

	// How a change is encoded as a string
	private static final Splitter ENCODED_CHANGE = Splitter.on('|').limit(3);

	/**
	 * Represents the last flight change for each plugin.
	 */
	private static final String METADATA_CHANGE = "flight_controller_change";
	/**
	 * The most recent change number. This is used to handle ties in priority.
	 */
	private static final String METADATA_CHANGE_NUMBER = "flight_controller_number";

	/**
	 * Represents different plugin priorities, in order of lowest to highest priority.
	 * @author Kristian
	 */
	public enum Priority {
		LOWEST,
		LOW,
		NORMAL,
		HIGH,
		HIGHEST
	}

	private final Plugin plugin;

	// Controller listener
	private Function<Object[], Object> serviceHook;
	private OnControllerChanged controllerListener;
	private Map<Player, Plugin> controllers = new MapMaker().weakKeys().weakValues().makeMap();

	/**
	 * Construct a new flight controller for a specific plugin.
	 * @param plugin - the plugin.
	 */
	public FlightController(Plugin plugin) {
		this.plugin = Preconditions.checkNotNull(plugin, "plugin cannot be NULL");
	}

	/**
	 * Submit a change request for the given player, permitting or denying flying if successful.
	 * <p>
	 * This will always remove any previous change requests submitted by this plugin.
	 * <p>
	 * A change request is enacted depending on {@link Priority}, and the time they were submitted if
	 * the priority is the same.
	 * @param player - the player to grant/deny flying.
	 * @param permitted - TRUE if the player is to be allowed to fly, FALSE otherwise.
	 * @return TRUE if we succeeded in changing the value, FALSE otherwise.
	 */
	public boolean changeFlight(Player player, boolean permitted) {
		return changeFlight(player, permitted, Priority.NORMAL);
	}

	/**
	 * Submit a change request for the given player, permitting or denying flying if successful.
	 * <p>
	 * This will always remove any previous change requests submitted by this plugin.
	 * <p>
	 * A change request is enacted depending on {@link Priority}, and the time they were submitted if
	 * the priority is the same.
	 * @param player - the player to grant/deny flying.
	 * @param permitted - TRUE if the player is to be allowed to fly, FALSE otherwise.
	 * @param priority - the priority of our change request.
	 * @return TRUE if we succeeded in changing the value, FALSE otherwise.
	 */
	public boolean changeFlight(Player player, boolean permitted, Priority priority) {
		int changeNumber = getChangeNumber(player) + 1;

		// Save metadata
		setMetadata(player, METADATA_CHANGE_NUMBER, changeNumber);
		setMetadata(player, METADATA_CHANGE, priority + "|" + changeNumber + "|" + permitted);
		return Objects.equal(updateFlight(player), plugin);
	}

	/**
	 * Update the {@link Player#setAllowFlight(boolean)} value depending on the metadata state.
	 * @param player - the player to update.
	 * @return The plugin whose value we set, or NULL if the value was not updated.
	 */
	private Plugin updateFlight(Player player) {
		Boolean state = null;
		Plugin bestPlugin = null;
		Priority bestPriority = Priority.LOW;
		int bestChangeNumber = Integer.MIN_VALUE;

		// Linear scan of the correct flight value - it's unlikely there will be more than 100 entries
		for (MetadataValue value : player.getMetadata(METADATA_CHANGE)) {
			try {
				String[] encoded = Iterables.toArray(ENCODED_CHANGE.split(value.asString()), String.class);
				Priority priority = Priority.valueOf(encoded[0]);
				int changeNumber = Integer.parseInt(encoded[1]);

				// See if the best result is less or equal to the current
				if (ComparisonChain.start().
						compare(bestPriority, priority).
						compare(bestChangeNumber, changeNumber).result() <= 0) {

					// Use this instead
					state = Boolean.parseBoolean(encoded[2]);
					bestPlugin = value.getOwningPlugin();
					bestPriority = priority;
					bestChangeNumber = changeNumber;
				}

			} catch (Exception e) {
				// Not our fault
				plugin.getLogger().log(Level.WARNING,
						"Detected invalid metadata (" + value.asString() + ") by " + value.getOwningPlugin(), e);
				player.removeMetadata(METADATA_CHANGE, value.getOwningPlugin());
			}
		}

		// We found a result
		if (state != null) {
			player.setAllowFlight(state);
		}
		notifyControllerChanged(player, bestPlugin);
		return bestPlugin;
	}

	/**
	 * Retrieve the plugin currently controlling the flight value of the given player.
	 * @param player - the player.
	 * @return The controller plugin.
	 */
	public Plugin getController(Player player) {
		Plugin plugin = controllers.get(player);

		if (plugin == null)
			plugin = updateFlight(player);
		return plugin;
	}

	/**
	 * Set the listener that is informed whenever the plugin controller switches for a player.
	 * @param controllerListener - the new controller listener, or NULL to disable.
	 */
	public void setControllerListener(OnControllerChanged controllerListener) {
		this.controllerListener = controllerListener;

		// Note that we appropriate Function for our purpose here,
		// preventing similar classes from doing the same
		if (controllerListener != null) {
			if (serviceHook == null) {
				// Prepare a "service" that intercepts the changed value
				serviceHook = new Function<Object[], Object>() {
					@Override
					public Object apply(@Nullable Object[] args) {
						invokeControllerChanged((Player) args[0], (Plugin) args[1]);
						return null;
					}
				};

				Bukkit.getServicesManager().register(Function.class, serviceHook,
						plugin, ServicePriority.Normal);
			}
		} else {
			if (serviceHook != null) {
				Bukkit.getServicesManager().unregister(serviceHook);
				serviceHook = null;
			}
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void notifyControllerChanged(Player player, Plugin plugin) {
		// Don't even try to get the generics working here ...
		for (RegisteredServiceProvider<Function> provider : Bukkit.getServicesManager().getRegistrations(Function.class)) {
			provider.getProvider().apply(new Object[] { player, plugin });
		}
	}

	/**
	 * Invoke the listener if the controller has changed.
	 * @param player - the player.
	 * @param controller - the new controller.
	 */
	private void invokeControllerChanged(Player player, Plugin controller) {
		if (controllerListener != null) {
			Plugin lastController = controller != null ?
					controllers.put(player, controller) :
					controllers.remove(player);

			// Inform the listener if the controller just changed
			if (!Objects.equal(lastController, controller)) {
				controllerListener.onChanged(player, controller);
			}
		}
	}

	/**
	 * Yield control over the flight value to the next change request in line.
	 * <p>
	 * The fallback value will only be used if there are no other change requests.
	 * @param player - the player we no longer wishes to modify.
	 * @param fallbackValue - flight value to set if there are no other change requests.
	 */
	public void yieldControl(Player player, boolean fallbackValue) {
		player.removeMetadata(METADATA_CHANGE, plugin);

		if (updateFlight(player) == null) {
			player.setAllowFlight(fallbackValue);
			invokeControllerChanged(player, null);
		}
	}

	/**
	 * Retrieve whether or not the player is permitted to fly.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public boolean getFlight(Player player) {
		return player.getAllowFlight();
	}

	/**
	 * Update the metadata value of the current plugin.
	 * @param target - the target.
	 * @param key - the metadata key.
	 * @param value - the new metadata value.
	 */
	private void setMetadata(Metadatable target, String key, Object value) {
		target.setMetadata(key, new FixedMetadataValue(plugin, value));
	}

	/**
	 * Retrieve the latest change number.
	 * @param player - the player.
	 * @return The latest change, or -1 if not found.
	 */
	private int getChangeNumber(Player player) {
		int latest = -1;

		for (MetadataValue value : player.getMetadata(METADATA_CHANGE_NUMBER)) {
			latest = Math.max(latest, value.asInt());
		}
		return latest;
	}
}