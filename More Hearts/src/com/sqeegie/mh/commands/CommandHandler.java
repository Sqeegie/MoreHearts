package com.sqeegie.mh.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sqeegie.mh.Colors;
import com.sqeegie.mh.MoreHearts;
import com.sqeegie.mh.commands.commands.AddAllWorldsCmd;
import com.sqeegie.mh.commands.commands.AddHeartsCmd;
import com.sqeegie.mh.commands.commands.AddWorldCmd;
import com.sqeegie.mh.commands.commands.RefreshCmd;
import com.sqeegie.mh.commands.commands.RemoveHeartsCmd;
import com.sqeegie.mh.commands.commands.RemoveWorldCmd;
import com.sqeegie.mh.commands.commands.ResetCmd;
import com.sqeegie.mh.commands.commands.SetHeartsCmd;
import com.sqeegie.mh.commands.commands.VersionCmd;
import com.sqeegie.mh.commands.commands.WorldsCmd;

public class CommandHandler implements CommandExecutor {

	private MoreHearts plugin;

	private List<CommandBase> commands;

	public CommandHandler() {
		commands = MoreHearts.newList();

		registerCommand(new AddAllWorldsCmd());
		registerCommand(new AddHeartsCmd());
		registerCommand(new AddWorldCmd());
		registerCommand(new RefreshCmd());
		registerCommand(new RemoveHeartsCmd());
		registerCommand(new RemoveWorldCmd());
		registerCommand(new ResetCmd());
		registerCommand(new SetHeartsCmd());
		registerCommand(new VersionCmd());
		registerCommand(new WorldsCmd());
	}

	public void registerCommand(CommandBase command) {
		commands.add(command);
	}

	public List<CommandBase> getCommands() {
		return new ArrayList<CommandBase>(commands);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		int argCount = args.length;
		
		if (argCount == 0) {
			if (cmdLabel.equalsIgnoreCase("mh") || cmdLabel.equalsIgnoreCase("morehearts")) {
				sender.sendMessage(Colors.SECONDARY + "MoreHearts help: " + Colors.MAIN + "<> - Required. " + Colors.SECONDARY + "[] - Optional");
				for (CommandBase command : commands) {
					List<String> description = command.getDescription();
					sender.sendMessage(Colors.MAIN + "/" + cmd.getName() + " " + command.getName() + " " + command.getPossibleArguments() + " - " + Colors.SECONDARY + description.get(0));
				}
				return true;
			}
			if (cmdLabel.equalsIgnoreCase("hearts") || cmdLabel.equalsIgnoreCase("h")) {
				if (sender instanceof Player) {
					Player player = MoreHearts.getPlayerByUsername(sender.getName());
					sender.sendMessage(Colors.MAIN + "You have " + Colors.SECONDARY + player.getHealth() / 2.0D + Colors.MAIN + "/" + Colors.SECONDARY + player.getMaxHealth() / 2.0D + Colors.MAIN + " hearts. (" + Colors.SECONDARY + player.getHealth() + Colors.MAIN + "/" + Colors.SECONDARY + player.getMaxHealth() + Colors.MAIN + " HP!)");
					return true;
				}
				else {
					sender.sendMessage(Colors.ERROR + "You must be a player to use this commands!");
				}
			}	
		}

		for (CommandBase command : commands) {

			if (command.isValidTrigger(args[0])) {

				if (!command.hasPermission(sender)) {
					sender.sendMessage(MoreHearts.getNoPerm());
					return true;
				}

				if (argCount - 1 >= command.getMinimumArguments()) {
					try {
						command.execute(sender, cmdLabel, Arrays.copyOfRange(args, 1, argCount));
					}
					catch (CommandException e) {
						sender.sendMessage(Colors.ERROR + e.getMessage());
					}
				}
				else {
					sender.sendMessage(Colors.ERROR + "Usage: /" + cmd + " " + command.getName() + " " + command.getPossibleArguments());
				}
				return true;
			}
		}

		sender.sendMessage(Colors.ERROR + "Unknown command. Type /mh for a list of commands.");
		return true;
	}

}
