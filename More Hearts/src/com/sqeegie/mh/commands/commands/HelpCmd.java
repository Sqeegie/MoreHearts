package com.sqeegie.mh.commands.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.sqeegie.mh.commands.CommandBase;
import com.sqeegie.mh.commands.CommandException;
import com.sqeegie.mh.commands.CommandHandler;
import com.sqeegie.mh.commands.Permissions;
import com.sqeegie.mh.utils.Colors;

public class HelpCmd extends CommandBase {
	
	public HelpCmd() {
		super("help", "commands");
		setPermission(Permissions.MOREHEARTS_PERM);
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
		sender.sendMessage(Colors.SECONDARY + "MoreHearts help: " + Colors.MAIN + "<> - Required. " + Colors.SECONDARY + "[] - Optional");
		sender.sendMessage(Colors.MAIN + "/hearts - " + Colors.SECONDARY + "Displays the number of health/hearts you have.");
		for (CommandBase command : CommandHandler.getCommands()) {
			List<String> description = command.getDescription();
			sender.sendMessage(Colors.MAIN + "/mh " + command.getName() + " " + command.getPossibleArguments() + " - " + Colors.SECONDARY + description.get(0));
		}
	}

	@Override
	public List<String> getDescription() {
		return Arrays.asList("Displays MoreHearts' commands.");
	}
}
