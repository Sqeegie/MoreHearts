package com.sqeegie.mh.commands.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import com.sqeegie.mh.MoreHearts;
import com.sqeegie.mh.commands.CommandBase;
import com.sqeegie.mh.commands.CommandException;
import com.sqeegie.mh.commands.Permissions;
import com.sqeegie.mh.utils.Colors;

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
			if (!MoreHearts.getConfiguration().getWorlds().contains(w.getName())) {
				MoreHearts.getConfiguration().addWorld(w.getName());
			}
			
			MoreHearts.getConfiguration().saveWorlds();
		}
		MoreHearts.refreshAllPlayers();
		sender.sendMessage(Colors.SECONDARY + "MoreHearts has been enabled in all loaded worlds!");
	}

	@Override
	public List<String> getDescription() {
		return Arrays.asList("Enables MoreHearts in all loaded worlds.");
	}
}
