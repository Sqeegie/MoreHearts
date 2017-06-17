package com.sqeegie.mh.commands.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sqeegie.mh.MoreHearts;
import com.sqeegie.mh.commands.CommandBase;
import com.sqeegie.mh.commands.CommandException;
import com.sqeegie.mh.commands.Permissions;
import com.sqeegie.mh.utils.Colors;

public class AddHeartsCmd extends CommandBase {

	public AddHeartsCmd() {
		super("add");
		setPermission(Permissions.ADDHEARTS_PERM);
	}

	@Override
	public String getPossibleArguments() {
		return "<player> <amount>";
	}

	@Override
	public int getMinimumArguments() {
		return 2;
	}

	@Override
	public int getMaximumArguments() {
		return 2;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void execute(CommandSender sender, String cmdName, String[] args) throws CommandException {
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
		if (!offlinePlayer.isOnline()) {
			sender.sendMessage(Colors.ERROR + "The player '" + args[0] + "' isn't online!");
		}
		else {
			try {
				Player player = (Player) offlinePlayer;
				double extraHeartsToAdd;

				try {
					extraHeartsToAdd = Double.parseDouble(args[1]);
				}
				catch (NumberFormatException formatException) {
					sender.sendMessage(Colors.ERROR + "Please enter a valid number!");
					return;
				}

				double currentExtraHearts = MoreHearts.getInstance().getConfig().getDouble("players." + player.getUniqueId() + ".extraHearts");
				double maximumAllowedHearts = MoreHearts.getConfiguration().getMaxHearts();
				double newMaxHearts = extraHeartsToAdd + currentExtraHearts + (MoreHearts.getConfiguration().getDefaultHealth() / 2.0d);

				if (newMaxHearts > maximumAllowedHearts) {
					sender.sendMessage("" + Colors.ERROR + "Cannot add that many hearts! The maximum hearts allowed is " + maximumAllowedHearts);
					return;
				}

				if (newMaxHearts > 0) {
					MoreHearts.getInstance().getConfig().set("players." + player.getUniqueId() + ".extraHearts", Double.valueOf(currentExtraHearts + extraHeartsToAdd));
					MoreHearts.getInstance().saveConfig();
					MoreHearts.refreshPlayer(player);
					sender.sendMessage("" + Colors.SECONDARY + extraHeartsToAdd + " heart(s) has been added to " + player.getName());
				}
				else {
					sender.sendMessage(Colors.ERROR + "You cannot set the maximum hearts to a non-positive amount!");
				}
			}
			catch (Exception e) {
				sender.sendMessage(Colors.ERROR + "Something went wrong! Check the console for details.");
				e.printStackTrace();
			}
		}
	}

	@Override
	public List<String> getDescription() {
		return Arrays.asList("Adds hearts to a player.");
	}
}
