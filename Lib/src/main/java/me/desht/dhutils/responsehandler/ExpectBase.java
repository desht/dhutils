package me.desht.dhutils.responsehandler;

import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.LogUtils;
import me.desht.dhutils.MiscUtil;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public abstract class ExpectBase {

	private ResponseHandler resp;
	private String playerName;

	public abstract void doResponse(String playerName);

	public ResponseHandler getResp() {
		return resp;
	}

	public void setResp(ResponseHandler resp) {
		this.resp = resp;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public void handleAction() {
		resp.handleAction(playerName, getClass());
	}

	public void cancelAction() {
		resp.cancelAction(playerName, getClass());
	}

	protected BukkitTask deferTask(final Player player, final Runnable task) {
		if (resp.getPlugin() == null) {
			throw new IllegalStateException("deferTask() called when response handler plugin not set");
		}
		return Bukkit.getScheduler().runTask(resp.getPlugin(), new Runnable() {
			@Override
			public void run() {
				try {
					task.run();
				} catch (DHUtilsException e) {
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
