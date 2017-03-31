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

public class RemoveHeartsCmd extends CommandBase {

	public RemoveHeartsCmd() {
		super("remove");
		setPermission(Permissions.REMOVEHEARTS_PERM);
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
	public void execute(CommandSender sender, String cmdName, String[] args) throws CommandException { // TODO: Check if removing negative numbers. (Fix maxHealth bypass.)
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
		if (!offlinePlayer.isOnline()) {
			sender.sendMessage(Colors.ERROR + "The player '" + args[0] + "' isn't online!");
		}
		else {
			try {
				Player player = (Player) offlinePlayer;
				double extraHeartsToRemove = Double.parseDouble(args[1]);
				double currentExtraHearts = MoreHearts.getInstance().getConfig().getDouble("players." + player.getUniqueId() + ".extraHearts");
				MoreHearts.getInstance().getConfig().set("players." + player.getUniqueId() + ".extraHearts", Double.valueOf(currentExtraHearts - extraHeartsToRemove));
				MoreHearts.getInstance().saveConfig();
				MoreHearts.refreshPlayer(player);
				sender.sendMessage("" + Colors.SECONDARY + extraHeartsToRemove + " hearts has been removed from " + player.getName());
			}
			catch (Exception e) {
				sender.sendMessage(Colors.ERROR + "Something went wrong! Did you enter a word instead of a number? Or try removing more health than there's health?");
			}
		}
	}

	@Override
	public List<String> getDescription() {
		return Arrays.asList("Removes hearts from a player.");
	}
}
