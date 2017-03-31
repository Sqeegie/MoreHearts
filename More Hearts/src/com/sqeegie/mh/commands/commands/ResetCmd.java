package com.sqeegie.mh.commands.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import com.sqeegie.mh.MoreHearts;
import com.sqeegie.mh.commands.CommandBase;
import com.sqeegie.mh.commands.CommandException;
import com.sqeegie.mh.commands.Permissions;
import com.sqeegie.mh.utils.Colors;

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
		if (args[1].equals(MoreHearts.getInstance().getConfig().getString("resetPassword"))) {
			// TODO: Fix this bug VVV. Doesn't actually reset without deleting the current config file first.
			MoreHearts.getInstance().saveDefaultConfig();
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
