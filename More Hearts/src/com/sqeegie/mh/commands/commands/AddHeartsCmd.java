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
import com.sqeegie.mh.utils.MoreHeartsUtil;

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
	public void execute(CommandSender sender, String cmdName, String[] args) throws CommandException {	
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
		if (!offlinePlayer.isOnline()) {
			sender.sendMessage(Colors.ERROR + "The player '" + args[0] + "' isn't online!");
		}
		else {
			try {
				Player player = (Player) offlinePlayer;
				double extraHeartsToAdd = Double.parseDouble(args[1]);
				double currentExtraHearts = MoreHearts.getInstance().getConfig().getDouble("players." + player.getUniqueId() + ".extraHearts");
				
				double maxHealthCheck = MoreHearts.getInstance().getConfig().getInt("maxHearts");
				double newMaxHealth = MoreHeartsUtil.roundToNearest(extraHeartsToAdd + currentExtraHearts);
				if (newMaxHealth > maxHealthCheck) {
					sender.sendMessage("" + Colors.ERROR + "Cannot add that many hearts! The maximum number of hearts is " + maxHealthCheck);
					return;
				}
				
				MoreHearts.getInstance().getConfig().set("players." + player.getUniqueId() + ".extraHearts", Double.valueOf(currentExtraHearts + extraHeartsToAdd));
				MoreHearts.getInstance().saveConfig();
				MoreHearts.refreshPlayer(player);
				sender.sendMessage("" + Colors.SECONDARY + extraHeartsToAdd + " hearts has been added to " + player.getName());
			}
			catch (Exception e) {
				sender.sendMessage(Colors.ERROR + "Something went wrong! Did you enter a word instead of a number? Or try setting the health to below zero?");
				e.printStackTrace();
			}
		}
	}

	@Override
	public List<String> getDescription() {
		return Arrays.asList("Adds hearts to a player.");
	}
}
