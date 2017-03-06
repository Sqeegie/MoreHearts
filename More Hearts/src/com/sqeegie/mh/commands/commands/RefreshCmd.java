package com.sqeegie.mh.commands.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.sqeegie.mh.Colors;
import com.sqeegie.mh.MoreHearts;
import com.sqeegie.mh.commands.CommandBase;
import com.sqeegie.mh.commands.CommandException;
import com.sqeegie.mh.commands.Permissions;

public class RefreshCmd extends CommandBase {
	
	public RefreshCmd() {
		super("refresh", "reload");
		setPermission(Permissions.REFRESH_PERM);
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
		MoreHearts.reloadConfiguration();
		MoreHearts.refreshPerms();
		MoreHearts.refreshWorlds();
		MoreHearts.refreshAllPlayers();
		sender.sendMessage(Colors.SECONDARY + "Everything has been refreshed!");
	}

	@Override
	public List<String> getDescription() {
		return Arrays.asList("Reloads config.");
	}
}
