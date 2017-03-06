package com.sqeegie.mh.commands.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import com.sqeegie.mh.Colors;
import com.sqeegie.mh.MoreHearts;
import com.sqeegie.mh.commands.CommandBase;
import com.sqeegie.mh.commands.CommandException;
import com.sqeegie.mh.commands.Permissions;

public class ResetCmd extends CommandBase {
	
	public ResetCmd() {
		super("reset");
		setPermission(Permissions.RESET_PERM);
	}

	@Override
	public String getPossibleArguments() {
		return "<password>";
	}

	@Override
	public int getMinimumArguments() {
		return 1;
	}
	
	@Override
	public int getMaximumArguments() {
		return 1;
	}

	@Override
	public void execute(CommandSender sender, String cmdName, String[] args) throws CommandException {
		if (args[1].equals(MoreHearts.getConfiguration().getString("resetPassword"))) {
			MoreHearts.getConfiguration().set("players", null);
			MoreHearts.getConfiguration().set("permissions", null);
			MoreHearts.getConfiguration().set("defaultHearts", Integer.valueOf(10));
			MoreHearts.getConfiguration().set("enabledIn", ((World) Bukkit.getWorlds().get(0)).getName());
			MoreHearts.getConfiguration().set("hideHearts", Boolean.valueOf(false));
			MoreHearts.getConfiguration().set("hideHeartsDisplayAmount", Integer.valueOf(10));
			MoreHearts.getConfiguration().set("enablePlayerHealthbars", Boolean.valueOf(false));
			MoreHearts.saveConfiguration();
			MoreHearts.refreshPerms();
			MoreHearts.refreshWorlds();
			MoreHearts.refreshAllPlayers();
			sender.sendMessage(Colors.SECONDARY + "Config has been reset!");
		}
		else {
			sender.sendMessage(Colors.ERROR + "Incorrect Password");
		}
	}

	@Override
	public List<String> getDescription() {
		return Arrays.asList("Deletes config.");
	}
}
