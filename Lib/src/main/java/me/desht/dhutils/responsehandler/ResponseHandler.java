package me.desht.dhutils.responsehandler;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ResponseHandler {

	private final Plugin plugin;
	private final ConcurrentMap<String, ExpectBase> exp = new ConcurrentHashMap<String, ExpectBase>();

	public ResponseHandler(Plugin plugin) {
		this.plugin = plugin;
	}

	public void expect(Player player, ExpectBase data) {
		expect(player, data, null);
	}

	private void expect(Player player, ExpectBase data, Player expectee) {
		data.setPlayerId(player.getUniqueId());
		data.setResponseHandler(this);
		if (expectee != null) {
			exp.put(genKey(expectee.getUniqueId(), data.getClass()), data);
		} else {
			exp.put(genKey(player.getUniqueId(), data.getClass()), data);
		}
	}

	private String genKey(UUID playerId, Class<? extends ExpectBase> c) {
		return playerId.toString() + ":" + c.getName();
	}

	public boolean isExpecting(Player player, Class<? extends ExpectBase> action) {
		return exp.containsKey(genKey(player.getUniqueId(), action));
	}

	public void handleAction(Player player, Class<? extends ExpectBase> action) {
		ExpectBase e = exp.get(genKey(player.getUniqueId(), action));
		cancelAction(player, action);
		e.doResponse(player.getUniqueId());
	}

	public void cancelAction(Player player, Class<? extends ExpectBase> action) {
		exp.remove(genKey(player.getUniqueId(), action));
	}

	public <T extends ExpectBase> T getAction(Player player, Class<T> action) {
		return action.cast(exp.get(genKey(player.getUniqueId(), action)));
	}

	public Plugin getPlugin() {
		return plugin;
	}

}
