package me.desht.dhutils.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.LogUtils;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.PermissionUtils;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.google.common.base.Joiner;

public class CommandManager {
	private static final List<String> EMPTY_STRING_LIST = new ArrayList<String>();

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

		List<AbstractCommand> possibleMatches = getPossibleMatches(label, args, false);

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

	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		LogUtils.fine("tab complete: sender=" + sender.getName() + ", cmd=" + command.getName() +
		              ", label=" + label + ", args=[" + Joiner.on(",").join(args) + "]");

		List<AbstractCommand> possibleMatches = getPossibleMatches(label, args, true);

		if (possibleMatches.size() == 0) {
			return noCompletions(sender);
		} else if (possibleMatches.size() == 1 && args.length > possibleMatches.get(0).getMatchedCommand().size()) {
			// tab completion to be done by the command itself
			LogUtils.fine("tab complete: pass to command: " + possibleMatches.get(0).getMatchedCommand());
			int from = possibleMatches.get(0).getMatchedCommand().size();
			try {
				return possibleMatches.get(0).onTabComplete(plugin, sender, subRange(args, from));
			} catch (DHUtilsException e) {
				MiscUtil.errorMessage(sender, e.getMessage());
				return noCompletions(sender);
			}
		} else {
			// tab completion done here; try to fill in the subcommand
			Set<String> completions = new HashSet<String>();
			for (AbstractCommand cmd : possibleMatches) {
				LogUtils.finer("add completion: " + cmd);
				completions.add(cmd.getMatchedCommand().subCommand(args.length - 1));
			}
			return MiscUtil.asSortedList(completions);
		}
	}

	private String[] subRange(String[] a, int from) {
		String[] res = new String[a.length - from];
		for (int i = 0; i < res.length; i++) {
			res[i] = a[from + i];
		}
		return res;
	}

	private List<AbstractCommand> getPossibleMatches(String label, String[] args, boolean partialOk) {
		List<AbstractCommand> possibleMatches = new ArrayList<AbstractCommand>();

		for (AbstractCommand cmd : cmdList) {
			if (cmd.matchesSubCommand(label, args, partialOk)) {
				possibleMatches.add(cmd);
			}
		}

		LogUtils.fine("found " + possibleMatches.size() + " possible matches for " + label);

		return possibleMatches;
	}

	static List<String> noCompletions() {
		return EMPTY_STRING_LIST;
	}

	static List<String> noCompletions(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player)sender;
			p.playSound(p.getLocation(), Sound.NOTE_BASS, 1.0f, 1.0f);
		}
		return EMPTY_STRING_LIST;
	}
}
