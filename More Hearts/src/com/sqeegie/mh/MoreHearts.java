package com.sqeegie.mh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.sqeegie.mh.commands.CommandHandler;
import com.sqeegie.mh.listeners.PlayerListener;
import com.sqeegie.mh.utils.MoreHeartsUtil;

/**
 * 
 * @authors roei12, Sqeegie
 *
 */

// TODO: Fix displaying maxhealth bug when permissions are set.
// TODO: Add compatibility with /heal commands.
// TODO: Fix config overwriting, loading, and comments.
// TODO: Fix reloading config.
// TODO: Add separate config files for each player

// TODO: Add MySQL support?
// TODO: Add an extra hearts clear command?
// TODO: Add additional command aliases?
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
	private Scoreboard scoreBoard;

	public static void log(String logs) {
		logger.info(logs);
	}

	/** Stuff to do when the plugin is being started */
	public void onEnable() {
		version = getDescription().getVersion();
		instance = this;
		instance.saveDefaultConfig();

		log("[MoreHearts] MoreHearts v" + version + " has been enabled!");

		if (getCommand("morehearts") == null) {
			logger.severe("Unabled to register commands! Disabling plugin...");
			instance.setEnabled(false);
		}

		getCommand("morehearts").setExecutor(new CommandHandler());
		getCommand("hearts").setExecutor(new CommandHandler());
		Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);

		scoreBoard = Bukkit.getScoreboardManager().getMainScoreboard();

		defaultHearts = (instance.getConfig().getDouble("defaultHearts") * 2.0D);
		vanishHearts = instance.getConfig().getBoolean("hideHearts");
		vanishHeartsDisplayAmount = (instance.getConfig().getDouble("hideHeartsDisplayAmount") * 2.0D);

		registerHealthBar();
		refreshPerms();
		refreshWorlds();
		refreshAllPlayers();
	}

	/** Stuff to do when the plugin is being shutdown */
	public void onDisable() {
		log("[MoreHearts] MoreHearts v" + getDescription().getVersion() + " has been disabled!");

		// Reset everybody's health back to MC's default
		Player[] arrayOfPlayers = Bukkit._INVALID_getOnlinePlayers();
		int numOfPlayers = arrayOfPlayers.length;
		for (int i = 0; i < numOfPlayers; i++) {
			Player player = arrayOfPlayers[i];

			instance.getConfig().set("players." + player.getUniqueId() + ".lastSeenAs", player.getName());
			instance.getConfig().set("players." + player.getUniqueId() + ".HP", Double.valueOf(MoreHeartsUtil.roundToNthPlace(player.getHealth(), 2)));
			player.setMaxHealth(20.0D);
		}

		// Save to config which worlds the plugin is enabled in
		String ws = (String) worlds.get(0);
		for (int i = 1; i < worlds.size(); i++) {
			ws = ws + "," + (String) worlds.get(i);
		}
		instance.getConfig().set("enabledIn", ws);
		saveConfig();

		unregisterHealthBar();
	}

	public static MoreHearts getInstance() {
		return instance;
	}

	public static String getVersion() {
		return version;
	}

	public static String getNoPerm() {
		return noPerm;
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
			MoreHeartsUtil.o = o;
			if (getConfig().getBoolean("enableHealthbarSymbol"))
				o.setDisplayName(MoreHeartsUtil.replaceSymbols(getConfig().getString("healthbarSymbol")));
			MoreHeartsUtil.refreshHealthbar();
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
			if (instance.getConfig().contains("players." + player.getUniqueId() + ".extraHearts")) {
				sum += instance.getConfig().getDouble("players." + player.getUniqueId() + ".extraHearts") * 2.0D;
			}
			else {
				instance.getConfig().set("players." + player.getUniqueId() + ".extraHearts", Integer.valueOf(0));
				instance.saveConfig();;
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
				 * double hp = getConfig().getDouble("players." +
				 * player.getUniqueId() + ".HP"); if (hp == 0.0D) {
				 * player.setHealth(sum); } else if (hp > sum) {
				 * player.setHealth(sum); } else { player.setHealth(hp); }
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
		for (String str : instance.getConfig().getConfigurationSection("permissions").getKeys(false)) {
			perms.put("morehearts." + str, Double.valueOf(instance.getConfig().getDouble("permissions." + str) * 2.0D));
		}
	}

	/** Save the list of all enabled worlds. */
	public static void saveWorlds() {
		String ws = (String) worlds.get(0);
		for (int a = 1; a < worlds.size(); a++) {
			ws = ws + "," + (String) worlds.get(a);
		}
		instance.getConfig().set("enabledIn", ws);
		instance.saveConfig();
	}

	/** Load the list of enabled worlds. */
	public static void refreshWorlds() {
		worlds.clear();
		String ws = instance.getConfig().getString("enabledIn");
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
