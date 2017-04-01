package com.sqeegie.mh.commands.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sqeegie.mh.MoreHearts;
import com.sqeegie.mh.commands.CommandBase;
import com.sqeegie.mh.commands.CommandException;
import com.sqeegie.mh.commands.Permissions;
import com.sqeegie.mh.utils.Colors;

public class AddWorldCmd extends CommandBase {

	public AddWorldCmd() {
		super("addworld");
		setPermission(Permissions.ADDWORLD_PERM);
	}

	@Override
	public String getPossibleArguments() {
		return "[world]";
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public int getMaximumArguments() {
		return 1;
	}

	@Override
	public void execute(CommandSender sender, String cmdName, String[] args) throws CommandException {
		if (args.length == 0 && sender instanceof Player) {
			Player player = MoreHearts.getPlayerByUsername(sender.getName());
			if (!MoreHearts.getConfiguration().getWorlds().contains(player.getWorld().getName())) {
				MoreHearts.getConfiguration().addWorld(player.getWorld().getName());
				player.sendMessage(Colors.MAIN + "The world '" + Colors.SECONDARY + player.getWorld().getName() + Colors.MAIN + "' has been added!");

				MoreHearts.refreshAllPlayers();
				MoreHearts.getConfiguration().saveWorlds();
			}
			else {
				throw new CommandException("MoreHearts is already enabled in this world!");
			}
		}
		else if (args.length > 0) { // Is using optional argument
			if (!MoreHearts.getConfiguration().getWorlds().contains(args[0])) {
				MoreHearts.getConfiguration().addWorld(args[0]);
				MoreHearts.getConfiguration().saveWorlds();
				sender.sendMessage(Colors.MAIN + "MoreHearts has been enabled in the world '" + Colors.SECONDARY + args[0] + Colors.MAIN + "'");
			}
			else {
				sender.sendMessage(Colors.ERROR + "MoreHearts is already enabled in this world!");
			}
		}
	}

	@Override
	public List<String> getDescription() {
		return Arrays.asList("Enables MoreHearts in a world.");
	}
}
