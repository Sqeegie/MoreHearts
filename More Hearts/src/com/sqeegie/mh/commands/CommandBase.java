package com.sqeegie.mh.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

public abstract class CommandBase {

	private String name;
	private String permission;
	private String[] aliases;

	public CommandBase(String name) {
		this(name, new String[0]);
	}

	public CommandBase(String name, String... aliases) {
		this.name = name;
		this.aliases = aliases;
	}

	public String getName() {
		return name;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public String getPermission() {
		return permission;
	}

	public final boolean hasPermission(CommandSender sender) {
		if (permission == null)
			return true;
		return sender.hasPermission(permission);
	}

	public abstract String getPossibleArguments();

	public abstract int getMinimumArguments();
	
	public abstract int getMaximumArguments();

	public abstract void execute(CommandSender sender, String label, String[] args) throws CommandException;

	public abstract List<String> getDescription();

	public final boolean isValidTrigger(String name) {
		if (this.name.equalsIgnoreCase(name)) {
			return true;
		}

		if (aliases != null) {
			for (String alias : aliases) {
				if (alias.equalsIgnoreCase(name)) {
					return true;
				}
			}
		}

		return false;
	}

}
