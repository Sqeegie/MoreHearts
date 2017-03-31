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

public class SetHeartsCmd extends CommandBase {

	public SetHeartsCmd() {
		super("set");
		setPermission(Permissions.SETHEARTS_PERM);
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
	public void execute(CommandSender sender, String cmdName, String[] args) throws CommandException {
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
		if (!offlinePlayer.isOnline()) {
			sender.sendMessage(Colors.ERROR + "The player '" + args[0] + "' isn't online!");
		}
		else {
			try {
				Player player = (Player) offlinePlayer;
				double finalHeartNumber = Double.parseDouble(args[1]);
				
				if (finalHeartNumber > MoreHearts.getInstance().getConfig().getInt("maxHearts")) {
					sender.sendMessage("" + Colors.ERROR + "Cannot set the health to that! The maximum number of hearts is " + MoreHearts.getInstance().getConfig().getInt("maxHearts"));
					return;
				}
				
				MoreHearts.getInstance().getConfig().set("players." + player.getUniqueId() + ".extraHearts", Double.valueOf(finalHeartNumber));
				MoreHearts.getInstance().saveConfig();
				MoreHearts.refreshPlayer(player);
				sender.sendMessage(Colors.SECONDARY + player.getName() + "'s extra hearts set to " + finalHeartNumber);
			}
			catch (Exception e) {
				sender.sendMessage(Colors.ERROR + "Something went wrong! Did you enter a word instead of a number? Or try setting the health to below zero?");
			}
		}
	}

	@Override
	public List<String> getDescription() {
		return Arrays.asList("Sets the hearts for a player.");
	}
}
