package me.desht.dhutils.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.MiscUtil;

public abstract class AbstractCommand {
	private String usage[];
	private int minArgs, maxArgs;
	private String permissionNode;
	private boolean quotedArgs;
	private final List<CommandRecord> cmdRecs = new ArrayList<AbstractCommand.CommandRecord>();
	private String matchedArgs[];

	public AbstractCommand(String label) {
		this(label, 0, Integer.MAX_VALUE);
	}

	public AbstractCommand(String label, int minArgs) {
		this(label, minArgs, Integer.MAX_VALUE);
	}

	public AbstractCommand(String label, int minArgs, int maxArgs) {
		quotedArgs = false;

		addAlias(label);
		this.minArgs = minArgs;
		this.maxArgs = maxArgs;
	}

	public abstract boolean execute(Plugin plugin, CommandSender sender, String[] args);

	public void addAlias(String label) {
		String[] fields = label.split(" ");
		cmdRecs.add(new CommandRecord(fields));
	}

	public boolean matchesSubCommand(String label, String[] args) {
		OUTER: for (CommandRecord rec : cmdRecs) {
			if (!label.equalsIgnoreCase(rec.command))
				continue;

			if (args.length < rec.subCommands.length)
				continue;

			for (int i = 0; i < rec.subCommands.length; i++) {
				//				System.out.println(String.format("match subcmd %d: [%s] <=> [%s]", i, args[i], rec.subCommands[i]));
				if (!args[i].startsWith(rec.subCommands[i])) {
					continue OUTER;
				}
			}
			return true;
		}

		return false;
	}

	public boolean matchesArgCount(String label, String args[]) {
		for (CommandRecord rec : cmdRecs) {
			if (!label.equalsIgnoreCase(rec.command))
				continue;

			int nArgs;
			if (isQuotedArgs()) {
				List<String> a = MiscUtil.splitQuotedString(combine(args, 0));
				nArgs = a.size() - rec.subCommands.length;
			} else {
				nArgs = args.length - rec.subCommands.length;
			}
			//		System.out.println(String.format("match %s, nArgs=%d min=%d max=%d", label, nArgs, minArgs, maxArgs));
			if (nArgs >= minArgs && nArgs <= maxArgs) {
				storeMatchedArgs(args, rec);
				return true;
			}
		}
		matchedArgs = null;
		return false;
	}

	private void storeMatchedArgs(String[] args, CommandRecord rec) {
		String[] result = new String[args.length - rec.subCommands.length];
		for (int i = rec.subCommands.length; i < args.length; i++) {
			result[i - rec.subCommands.length] = args[i];
		}
		if (isQuotedArgs()) {
			List<String>a = MiscUtil.splitQuotedString(combine(result, 0));
			matchedArgs = a.toArray(new String[a.size()]);
		} else {
			matchedArgs = result;
		}
	}

	void showUsage(CommandSender sender) {
		if (usage != null) {
			for (int i = 0; i < usage.length; i++) {
				if (i == 0) {
					MiscUtil.errorMessage(sender, "Usage: " + usage[i]);
				} else {
					MiscUtil.errorMessage(sender, "         " + usage[i]);
				}
			}
		}
	}

	String[] getArgs() {
		return matchedArgs;
	}

	protected void setPermissionNode(String node) {
		this.permissionNode = node;
	}

	protected void setUsage(String usage) {
		this.usage = new String[] { usage };
	}

	protected void setUsage(String[] usage) {
		this.usage = usage;
	}

	protected String[] getUsage() {
		return usage;
	}

	protected String getPermissionNode() {
		return permissionNode;
	}

	public boolean isQuotedArgs() {
		return quotedArgs;
	}

	public void setQuotedArgs(boolean usesQuotedArgs) {
		this.quotedArgs = usesQuotedArgs;
	}

	protected void notFromConsole(CommandSender sender) throws DHUtilsException {
		if (!(sender instanceof Player)) {
			throw new DHUtilsException("This command can't be run from the console.");
		}	
	}

	static String combine(String[] args, int idx) {
		return combine(args, idx, args.length - 1);
	}

	static String combine(String[] args, int idx1, int idx2) {
		StringBuilder result = new StringBuilder();
		for (int i = idx1; i <= idx2 && i < args.length; i++) {
			result.append(args[i]);
			if (i < idx2) {
				result.append(" ");
			}
		}
		return result.toString();
	}

	static Map<String, String> parseCommandOptions(String[] args, int start) {
		Map<String, String> res = new HashMap<String, String>();

		Pattern pattern = Pattern.compile("^-(.+)"); //$NON-NLS-1$

		for (int i = start; i < args.length; ++i) {
			Matcher matcher = pattern.matcher(args[i]);
			if (matcher.find()) {
				String opt = matcher.group(1);
				try {
					res.put(opt, args[++i]);
				} catch (ArrayIndexOutOfBoundsException e) {
					res.put(opt, null);
				}
			}
		}
		return res;
	}

	private class CommandRecord {
		private final String command;
		private final String subCommands[];
		
		public CommandRecord(String[] fields) {
			this.command = fields[0];
			this.subCommands = new String[fields.length - 1];
			for (int i = 1; i < fields.length; i++) {
				subCommands[i - 1] = fields[i];
			}
		}
	}
}
