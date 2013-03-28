package me.desht.dhutils.commands;

import java.util.ArrayList;
import java.util.List;

import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.LogUtils;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.PermissionUtils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class CommandManager {
	private final List<AbstractCommand> cmdList = new ArrayList<AbstractCommand>();
	private Plugin plugin;

	public CommandManager(Plugin plugin) {
		this.plugin = plugin;
	}

	public void registerCommand(AbstractCommand cmd) {
		cmdList.add(cmd);
	}

	public boolean dispatch(CommandSender sender, String label, String[] args) {
		boolean res = true;

		List<AbstractCommand> possibleMatches = new ArrayList<AbstractCommand>();

		for (AbstractCommand cmd : cmdList) {
			if (cmd.matchesSubCommand(label, args)) {
				possibleMatches.add(cmd);
			}
		}

		LogUtils.finer("found " + possibleMatches.size() + " possible matches for " + label);

		if (possibleMatches.size() == 1) {
			// good - a unique match
			AbstractCommand cmd = possibleMatches.get(0);
			if (cmd.matchesArgCount(label, args)) {
				if (cmd.getPermissionNode() != null) {
					PermissionUtils.requirePerms(sender, cmd.getPermissionNode());
				}
				res = cmd.execute(plugin, sender, cmd.getMatchedArgs());
			} else {
				cmd.showUsage(sender);
			}
		} else if (possibleMatches.size() == 0) {
			// no match
			res = false;
		} else {
			// multiple possible matches
			MiscUtil.errorMessage(sender, possibleMatches.size() + " possible matching commands:");
			for (AbstractCommand cmd : possibleMatches) {
				cmd.showUsage(sender);
			}
		}
		return res;
	}

	public boolean dispatch(CommandSender sender, Command command, String label, String[] args) {
		try {
			return dispatch(sender, command.getName(), args);
		} catch (DHUtilsException e) {
			MiscUtil.errorMessage(sender, e.getMessage());
			return true;
		}
	}
}
