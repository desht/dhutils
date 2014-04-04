package me.desht.dhutils.responsehandler;

import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.LogUtils;
import me.desht.dhutils.MiscUtil;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public abstract class ExpectBase {

	private ResponseHandler resp;
	private UUID playerId;

	public abstract void doResponse(UUID playerId);

	public ResponseHandler getResponseHandler() {
		return resp;
	}

	public void setResponseHandler(ResponseHandler resp) {
		this.resp = resp;
	}

	public UUID getPlayerId() {
		return playerId;
	}

	public void setPlayerId(UUID playerId) {
		this.playerId = playerId;
	}

	public void handleAction(Player player) {
		resp.handleAction(player, getClass());
	}

	public void cancelAction(Player player) {
		resp.cancelAction(player, getClass());
	}

	protected BukkitTask deferTask(final UUID playerId, final Runnable task) {
		if (resp.getPlugin() == null) {
			throw new IllegalStateException("deferTask() called when response handler plugin not set");
		}
		return Bukkit.getScheduler().runTask(resp.getPlugin(), new Runnable() {
			@Override
			public void run() {
				try {
					task.run();
				} catch (DHUtilsException e) {
					Player player = Bukkit.getPlayer(playerId);
					if (player != null) {
						MiscUtil.errorMessage(player, e.getMessage());
					} else {
						LogUtils.warning(e.getMessage());
					}
				}
			}
		});
	}
}
