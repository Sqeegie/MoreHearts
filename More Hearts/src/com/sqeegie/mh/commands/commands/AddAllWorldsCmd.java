package com.sqeegie.mh.commands.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sqeegie.mh.Colors;
import com.sqeegie.mh.MoreHearts;
import com.sqeegie.mh.commands.CommandBase;
import com.sqeegie.mh.commands.CommandException;
import com.sqeegie.mh.commands.Permissions;

public class AddAllWorldsCmd extends CommandBase {

	public AddAllWorldsCmd() {
		super("addallworlds");
		setPermission(Permissions.ADDALLWORLDS_PERM);
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
		for (World w : Bukkit.getWorlds()) {
			if (!MoreHearts.getWorlds().contains(w.getName())) {
				MoreHearts.addWorld(w.getName());
			}
			
			MoreHearts.saveWorlds();
		}
		MoreHearts.refreshAllPlayers();
		sender.sendMessage(Colors.SECONDARY + "MoreHearts has been enabled in all loaded worlds!");
	}

	@Override
	public List<String> getDescription() {
		return Arrays.asList("Enables MoreHearts in all loaded worlds.");
	}
}
