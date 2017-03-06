package com.sqeegie.mh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.sqeegie.mh.commands.CommandHandler;

/**
 * 
 * @authors roei12, Sqeegie
 *
 */

// TODO: Add max health limit to config.
// TODO: Add compatibility with /heal commands.
// TODO: Add healthbar symbol.
// TODO: Add onEvent from refactor branch.

// TODO: Add additional command aliases?
// TODO: Add separate config files for each player?
// TODO: Add more configurability. (Cause why not?)
// TODO: Decrease the precision of the health value saved to file?

@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
public class MoreHearts extends JavaPlugin {
	
	private static MoreHearts instance;
	
	/** Default values */
	static Logger logger = Logger.getLogger("minecraft");
	static HashMap<String, Double> perms = new HashMap();
	static double defaultHearts = 20.0d;
	static boolean vanishHearts = false;
	static double vanishHeartsDisplayAmount = defaultHearts;
	private static ArrayList<String> worlds = new ArrayList();
	private static String noPerm = ChatColor.RED + "You don't have permission to use this command!";
	private static String version;
	private static FileConfiguration config;
	private Scoreboard scoreBoard;
	
	public static void log(String logs) {
		logger.info(logs);
	}
	
	/** Stuff to do when the plugin is being started */
	public void onEnable() {
		version = getDescription().getVersion();
		config = getConfig();
		instance = this;
		
		log("[MoreHearts] MoreHearts v" + version + " has been enabled!");

		if (getCommand("morehearts") == null) {
			logger.severe("Unabled to register commands! Disabling plugin...");
			instance.setEnabled(false);
		}
		
		getCommand("morehearts").setExecutor(new CommandHandler());
		getCommand("hearts").setExecutor(new CommandHandler());
		Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);

		this.scoreBoard = Bukkit.getScoreboardManager().getMainScoreboard();

		// Create default config if non-existent
		if (!getConfig().contains("players")) {
			int randomPassword = 1000 + (int) (Math.random() * 999999.0D);
			getConfig().createSection("players");
			getConfig().createSection("permissions");
			getConfig().set("defaultHearts", Integer.valueOf(10));
			getConfig().set("enabledIn", ((World) Bukkit.getWorlds().get(0)).getName());
			getConfig().set("resetPassword", Integer.valueOf(randomPassword));
			getConfig().set("hideHearts", Boolean.valueOf(false));
			getConfig().set("hideHeartsDisplayAmount", Integer.valueOf(10));
			getConfig().set("enablePlayerHealthbars", Boolean.valueOf(false));
			saveConfig();
		}

		defaultHearts = (getConfig().getDouble("defaultHearts") * 2.0D);
		vanishHearts = getConfig().getBoolean("hideHearts");
		vanishHeartsDisplayAmount = (getConfig().getDouble("hideHeartsDisplayAmount") * 2.0D);

		refreshPerms();
		refreshWorlds();
		registerHealthBar();

		Player[] arrayOfPlayers = Bukkit._INVALID_getOnlinePlayers();
		int numOfPlayers = arrayOfPlayers.length;
		for (int i = 0; i < numOfPlayers; i++) {
			Player player = arrayOfPlayers[i];
			refreshPlayer(player);
		}
	}

	/** Stuff to do when the plugin is being shutdown */
	public void onDisable() {
		log("[MoreHearts] MoreHearts v" + getDescription().getVersion() + " has been disabled!");

		// Reset everybody's health back to MC's default
		Player[] arrayOfPlayers = Bukkit._INVALID_getOnlinePlayers();
		int numOfPlayers = arrayOfPlayers.length;
		for (int i = 0; i < numOfPlayers; i++) {
			Player player = arrayOfPlayers[i];

			getConfig().set("players." + player.getUniqueId() + ".lastSeenAs", player.getName());
			getConfig().set("players." + player.getUniqueId() + ".HP", Double.valueOf(player.getHealth()));
			player.setMaxHealth(20.0D);
		}

		// Save to config which worlds the plugin is enabled in
		String ws = (String) worlds.get(0);
		for (int i = 1; i < worlds.size(); i++) {
			ws = ws + "," + (String) worlds.get(i);
		}
		getConfig().set("enabledIn", ws);
		saveConfig();

		unregisterHealthBar();
	}

	public static MoreHearts getInstance() {
		return instance;
	}

	public static void saveConfiguration() {
		getInstance().saveConfig();
	}
	
	public static void reloadConfiguration() {
		getInstance().reloadConfig();
	}
	
	public static String getVersion() {
		return version;
	}

	public static String getNoPerm() {
		return noPerm;
	}

	public static FileConfiguration getConfiguration() {
		return config;
	}

	public static ArrayList<String> getWorlds() {
		return worlds;
	}
	
	public static void addWorld(String world) {
		MoreHearts.worlds.add(world);
	}
	
	public static void removeWorld(String world) {
		MoreHearts.worlds.remove(world);
	}

	public void registerHealthBar() {
		if (this.scoreBoard.getObjective("health") != null) {
			this.scoreBoard.getObjective("health").unregister();
		}
		if (getConfig().getBoolean("enablePlayerHealthbars")) {
			Objective o = this.scoreBoard.registerNewObjective("health", "health");
			o.setDisplayName("HP");
			o.setDisplaySlot(DisplaySlot.BELOW_NAME);
		}
	}

	public void unregisterHealthBar() {
		Set<Objective> sidebar = scoreBoard.getObjectives();
		for (Objective o : sidebar) {
			if (o.getName().contains("health")) {
				o.unregister();
			}
		}
	}
	
	public static <T> List<T> newList() {
		return new ArrayList<T>();
	}

	/** Loads a single player's config properties. */
	public static void refreshPlayer(Player player) {
		double sum = defaultHearts;
		if (worlds.contains(player.getWorld().getName())) {
			if (config.contains("players." + player.getUniqueId() + ".extraHearts")) {
				sum += config.getDouble("players." + player.getUniqueId() + ".extraHearts") * 2.0D;
			}
			else {
				config.set("players." + player.getUniqueId() + ".extraHearts", Integer.valueOf(0));
				saveConfiguration();
			}
			for (String str : perms.keySet()) {
				if (player.isPermissionSet(str)) {
					sum += ((Double) perms.get(str)).doubleValue();
				}
			}
			player.setMaxHealth(sum);
			if (sum > defaultHearts) { // Has extra hearts - enable/disable hide hearts for that player
				player.setHealthScale(vanishHeartsDisplayAmount);
				player.setHealthScaled(vanishHearts);	
			}
			if (sum < vanishHeartsDisplayAmount) {
				player.setHealthScale(sum);
			}
			if (!player.isDead()) {
				player.setHealth(player.getMaxHealth());
				/*
				double hp = getConfig().getDouble("players." + player.getUniqueId() + ".HP");
				if (hp == 0.0D) {
					player.setHealth(sum);
				}
				else if (hp > sum) {
					player.setHealth(sum);
				}
				else {
					player.setHealth(hp);
				}
				*/
			}
		}
		else {
			player.setMaxHealth(20.0D);
		}
	}

	/** Load custom permission parameters from config */
	public static void refreshPerms() {
		perms.clear();
		for (String str : config.getConfigurationSection("permissions").getKeys(false)) {
			perms.put("morehearts." + str, Double.valueOf(config.getDouble("permissions." + str) * 2.0D));
		}
	}

	/** Save the list of all enabled worlds. */
	public static void saveWorlds() {
		String ws = (String) worlds.get(0);
		for (int a = 1; a < worlds.size(); a++) {
			ws = ws + "," + (String) worlds.get(a);
		}
		config.set("enabledIn", ws);
		saveConfiguration();
	}

	/** Load the list of enabled worlds. */
	public static void refreshWorlds() {
		worlds.clear();
		String ws = config.getString("enabledIn");
		if (ws != null || !ws.isEmpty()) {
			if (ws.contains(",")) {
				String[] Eworlds = ws.split(",");
				String[] arrayOfString1;
				int j = (arrayOfString1 = Eworlds).length;
				for (int i = 0; i < j; i++) {
					String str = arrayOfString1[i];

					worlds.add(str);
				}
			}
			else {
				worlds.add(ws);
			}
		}
	}

	/** Loads the list of all players. */
	public static void refreshAllPlayers() {
		Player[] arrayOfPlayers = Bukkit._INVALID_getOnlinePlayers();
		int numOfPlayers = arrayOfPlayers.length;
		for (int i = 0; i < numOfPlayers; i++) {
			Player players = arrayOfPlayers[i];
			refreshPlayer(players);
		}
	}

	/**
	 * Gets a player object via an username.
	 */
	public static Player getPlayerByUsername(String username) {
		Player player = getInstance().getServer().getPlayer(username);
		return player; // Can return a null value if the username is invalid
	}
}
