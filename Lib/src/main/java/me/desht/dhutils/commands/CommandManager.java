package me.desht.dhutils.commands;

import java.util.ArrayList;
import java.util.List;

import me.desht.dhutils.DHUtilsException;
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
		boolean res = false;
		for (AbstractCommand cmd : cmdList) {
			if (cmd.matchesSubCommand(label, args)) {
				if (cmd.matchesArgCount(label, args)) {
					if (cmd.getPermissionNode() != null) {
						PermissionUtils.requirePerms(sender, cmd.getPermissionNode());
					}
					String[] actualArgs = cmd.getArgs();
					res = cmd.execute(plugin, sender, actualArgs);
				} else {
					cmd.showUsage(sender);
					res = true;
				}
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
