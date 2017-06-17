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
	@SuppressWarnings("deprecation")
	public void execute(CommandSender sender, String cmdName, String[] args) throws CommandException {
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
		if (!offlinePlayer.isOnline()) {
			sender.sendMessage(Colors.ERROR + "The player '" + args[0] + "' isn't online!");
		}
		else {
			try {
				Player player = (Player) offlinePlayer;
				double finalHeartNumber;
				
				try { // Pretty sure this method is frowned upon. . . but screw it. I'm using it anyway
					finalHeartNumber = Double.parseDouble(args[1]);
				}
				catch (NumberFormatException formatException) {
					sender.sendMessage(Colors.ERROR + "Please enter a valid number!");
					return;
				}
				
				if (finalHeartNumber > MoreHearts.getConfiguration().getMaxHearts()) {
					sender.sendMessage("" + Colors.ERROR + "Cannot set the maxiumum hearts to that! The maximum hearts allowed is " + MoreHearts.getConfiguration().getMaxHearts());
					return;
				}
				
				if (finalHeartNumber > 0) {
					MoreHearts.getInstance().getConfig().set("players." + player.getUniqueId() + ".extraHearts", Double.valueOf(finalHeartNumber - (MoreHearts.getConfiguration().getDefaultHealth() / 2.0d)));
					MoreHearts.getInstance().saveConfig();
					MoreHearts.refreshPlayer(player);
					sender.sendMessage(Colors.SECONDARY + player.getName() + "'s hearts is now set to " + finalHeartNumber);	
				}
				else {
					sender.sendMessage(Colors.ERROR + "You cannot set the maximum hearts to a non-positive amount!");
				}
			}
			catch (Exception e) {
				sender.sendMessage(Colors.ERROR + "Something went wrong! See console for details.");
				e.printStackTrace();
			}
		}
	}

	@Override
	public List<String> getDescription() {
		return Arrays.asList("Sets the max hearts for a player.");
	}
}
