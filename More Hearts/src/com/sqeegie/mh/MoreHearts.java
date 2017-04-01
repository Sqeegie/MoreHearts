package com.sqeegie.mh;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.sqeegie.mh.commands.CommandHandler;
import com.sqeegie.mh.configuration.MoreHeartsConfiguration;
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

@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
public class MoreHearts extends JavaPlugin {

	private static MoreHearts instance;
	private MoreHeartsConfiguration config;

	/** Default values */
	static Logger logger = Logger.getLogger("minecraft");
	private static String noPermLocale = ChatColor.RED + "You don't have permission to use this command!";
	private static String version;
	private Scoreboard scoreBoard;

	public static void log(String logs) {
		logger.info(logs);
	}

	/** Stuff to do when the plugin is being started */
	public void onEnable() {
		version = getDescription().getVersion();
		instance = this;

		File configFile = new File(this.getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			saveDefaultConfig();
		}
		Path dataFolder = getDataFolder().toPath();
		Configuration checkConfig = YamlConfiguration.loadConfiguration(configFile);
		checkConfigVersions(checkConfig, dataFolder);
		config = new MoreHeartsConfiguration(getConfig());
		
		if (getCommand("morehearts") == null) {
			logger.severe("Unabled to register commands! Disabling plugin...");
			instance.setEnabled(false);
		}
		getCommand("morehearts").setExecutor(new CommandHandler());
		getCommand("hearts").setExecutor(new CommandHandler());
		
		Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);

		scoreBoard = Bukkit.getScoreboardManager().getMainScoreboard();

		registerHealthBar();
		config.refreshPerms();
		config.refreshWorlds();
		refreshAllPlayers();
		log("[MoreHearts] MoreHearts v" + version + " has been enabled!");
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

		config.saveWorlds();

		unregisterHealthBar();
	}

	public static MoreHearts getInstance() {
		return instance;
	}

	public static String getVersion() {
		return version;
	}

	public static String getNoPerm() {
		return noPermLocale;
	}

	public static MoreHeartsConfiguration getConfiguration() {
		return getInstance().config;
	}

	private void checkConfigVersions(Configuration config, Path dataFolder) {
		if (config.getInt("config-version", 0) < MoreHeartsConfiguration.CURRENT_CONFIG_VERSION) {
			Path configSource = dataFolder.resolve(MoreHeartsConfiguration.DESTINATION_FILE_NAME);
			Path configTarget = dataFolder.resolve("config_old.yml");

			try {
				Files.move(configSource, configTarget, StandardCopyOption.REPLACE_EXISTING);
				URL configResource = getClass().getResource(MoreHeartsConfiguration.CLASSPATH_RESOURCE_NAME);

				copyResource(configResource, configSource.toFile());

				ConsoleCommandSender sender = Bukkit.getConsoleSender();
				sender.sendMessage(ChatColor.RED + "Due to a MoreHearts update your old configuration has been renamed");
				sender.sendMessage(ChatColor.RED + "to config_old.yml and a new one has been generated. Make sure to");
				sender.sendMessage(ChatColor.RED + "apply your old changes to the new config");
			}
			catch (IOException e) {
				getLogger().log(Level.SEVERE, "Could not create updated configuration due to an IOException", e);
			}
		}
	}

	public static void copyResource(URL resourceUrl, File destination) throws IOException {
		URLConnection connection = resourceUrl.openConnection();

		if (!destination.exists()) {
			destination.getParentFile().mkdirs();
			destination.createNewFile();
		}

		final int bufferSize = 1024;

		try (InputStream inStream = connection.getInputStream();
				FileOutputStream outStream = new FileOutputStream(destination)) {
			byte[] buffer = new byte[bufferSize];

			int read;
			while ((read = inStream.read(buffer)) > 0) {
				outStream.write(buffer, 0, read);
			}
		}
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
		double sum = getConfiguration().getDefaultHealth();
		if (getConfiguration().getWorlds().contains(player.getWorld().getName())) {
			if (instance.getConfig().contains("players." + player.getUniqueId() + ".extraHearts")) {
				sum += instance.getConfig().getDouble("players." + player.getUniqueId() + ".extraHearts") * 2.0D;
			}
			else {
				instance.getConfig().set("players." + player.getUniqueId() + ".extraHearts", Integer.valueOf(0));
				instance.saveConfig();;
			}
			for (String str : getConfiguration().getPerms().keySet()) {
				if (player.isPermissionSet(str)) {
					sum += ((Double) getConfiguration().getPerms().get(str)).doubleValue();
				}
			}
			player.setMaxHealth(sum);
			if (sum > getConfiguration().getDefaultHealth()) { // Has extra hearts - enable/disable hide hearts for that player
				player.setHealthScale(getConfiguration().getHideHeartAmount());
				player.setHealthScaled(getConfiguration().isHideHeartsOn());
			}
			if (sum < getConfiguration().getHideHeartAmount()) {
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
