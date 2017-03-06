package com.sqeegie.mh.commands.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.sqeegie.mh.Colors;
import com.sqeegie.mh.MoreHearts;
import com.sqeegie.mh.commands.CommandBase;
import com.sqeegie.mh.commands.CommandException;
import com.sqeegie.mh.commands.Permissions;

public class VersionCmd extends CommandBase {
	
	public VersionCmd() {
		super("version", "v");
		setPermission(Permissions.VERSION_PERM);
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
		sender.sendMessage(Colors.MAIN + "MoreHearts version: " + Colors.SECONDARY + "v" + MoreHearts.getVersion());
	}

	@Override
	public List<String> getDescription() {
		return Arrays.asList("Displays MoreHearts' version number.");
	}
}
