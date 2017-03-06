package com.sqeegie.mh.commands.commands;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.sqeegie.mh.Colors;
import com.sqeegie.mh.MoreHearts;
import com.sqeegie.mh.commands.CommandBase;
import com.sqeegie.mh.commands.CommandException;
import com.sqeegie.mh.commands.Permissions;

public class WorldsCmd extends CommandBase {
	
	public WorldsCmd() {
		super("worlds");
		setPermission(Permissions.WORLDS_PERM);
	}

	@Override
	public String getPossibleArguments() {
		return "";
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}
	
	@Override
	public int getMaximumArguments() {
		return 0;
	}

	@Override
	public void execute(CommandSender sender, String cmdName, String[] args) throws CommandException {
		String str;
		boolean b = true;
		sender.sendMessage(Colors.SECONDARY + "MoreHearts is enabled in:");
		for (Iterator localIterator = MoreHearts.getWorlds().iterator(); localIterator.hasNext();) {
			str = (String) localIterator.next();
			if (b) {
				sender.sendMessage(Colors.MAIN + "- " + str);
			}
			else {
				sender.sendMessage(Colors.SECONDARY + "- " + str);
			}
			b = !b;
		}
	}

	@Override
	public List<String> getDescription() {
		return Arrays.asList("Displays a list of all the worlds MoreHearts is enabled in.");
	}
}
