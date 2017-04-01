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

public class RemoveWorldCmd extends CommandBase {

	public RemoveWorldCmd() {
		super("removeworld");
		setPermission(Permissions.REMOVEWORLD_PERM);
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

			if (MoreHearts.getConfiguration().getWorlds().contains(player.getWorld().getName())) {
				MoreHearts.getConfiguration().removeWorld(player.getWorld().getName());
				player.sendMessage(Colors.MAIN + "MoreHearts has been disabled in the world '" + Colors.SECONDARY + player.getWorld().getName() + Colors.MAIN + "'");

				MoreHearts.refreshAllPlayers();
				MoreHearts.getConfiguration().saveWorlds();
			}
			else {
				sender.sendMessage(Colors.ERROR + "MoreHearts is already disabled in this world!");
			}
		}
		else if (args.length > 0) { // Is using optional argument
			if (MoreHearts.getConfiguration().getWorlds().contains(args[0])) {
				MoreHearts.getConfiguration().removeWorld(args[0]);
				MoreHearts.getConfiguration().saveWorlds();
				sender.sendMessage(Colors.MAIN + "MoreHearts has been disabled in the world '" + Colors.SECONDARY + args[0] + Colors.MAIN + "'");
			}
			else {
				sender.sendMessage(Colors.ERROR + "MoreHearts is already disabled in this world!");
			}

		}
	}

	@Override
	public List<String> getDescription() {
		return Arrays.asList("Disables MoreHearts in a world.");
	}
}
