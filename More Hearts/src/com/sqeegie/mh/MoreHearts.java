package com.sqeegie.mh;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
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

@SuppressWarnings({ "deprecation" })
public class MoreHearts extends JavaPlugin {

	private static MoreHearts instance;
	private MoreHeartsConfiguration config;

	/** Default values */
	static Logger logger = Logger.getLogger("minecraft");
	private static String noPermLocale = ChatColor.RED + "You don't have permission to use this command!";
	private static String version;
	private Scoreboard scoreBoard;

	public static void log(String logs) {
		logger.info("[MoreHearts] " + logs);
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
		log("MoreHearts v" + version + " has been enabled!");
	}

	/** Stuff to do when the plugin is being shutdown */
	public void onDisable() {
		log("MoreHearts v" + getDescription().getVersion() + " has been disabled!");

		// Reset everybody's health back to MC's default
		Collection<? extends Player> collectionOfPlayers = Bukkit.getOnlinePlayers();
		for (Player player : collectionOfPlayers) {
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
		if (!config.contains("config-version") || config.getInt("config-version", 0) < MoreHeartsConfiguration.CURRENT_CONFIG_VERSION) {
			Path configSource = dataFolder.resolve(MoreHeartsConfiguration.DESTINATION_FILE_NAME);
			Path configTarget = dataFolder.resolve("config_old.yml");

			try {
				Files.move(configSource, configTarget, StandardCopyOption.REPLACE_EXISTING);
				URL configResource = getClass().getResource(MoreHeartsConfiguration.CLASSPATH_RESOURCE_NAME);

				copyResource(configResource, configSource.toFile());
				
				instance.saveDefaultConfig(); // Temporary solution until automatic config porting is finished
				
				//portConfig();
				
				ConsoleCommandSender sender = Bukkit.getConsoleSender();
				sender.sendMessage(ChatColor.RED + "Due to a MoreHearts update your old configuration has been renamed");
				sender.sendMessage(ChatColor.RED + "to config_old.yml and a new one has been generated. Make sure to");
				sender.sendMessage(ChatColor.RED + "reconfigure MoreHearts to your previous setup.");
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

	public static void portConfig() {
		
		InputStream configStream = getInstance().getResource("config_old.yml");
		YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(configStream));
		
		if (!yamlConfig.contains("config-version")) { // Really old config
			// Port global options
			ConfigurationSection globalSection = getInstance().getConfig().getConfigurationSection("global");
			
			globalSection.set("defaultHearts", yamlConfig.getInt("defaultHearts", 10)); 
			globalSection.set("maxHearts", yamlConfig.getInt("maxHearts", 250));
			globalSection.set("resetPassword", yamlConfig.getInt("resetPassword", 465004));
			globalSection.set("hideHearts", yamlConfig.getBoolean("hideHearts", false));
			globalSection.set("hideHeartsDisplayAmount", yamlConfig.getDouble("hideHeartsDisplayAmount", 10));
			globalSection.set("enablePlayerHealthbars", yamlConfig.getBoolean("enablePlayerHealthbars", false));
			globalSection.set("useHeartsAsHealthbarType", yamlConfig.getBoolean("useHeartsAsHealthbarType", true));
			globalSection.set("healthbarSymbol", yamlConfig.getString("healthbarSymbol", " &c❤"));
			globalSection.set("enableHealthbarSymbol", yamlConfig.getBoolean("enableHealthbarSymbol", true));
			globalSection.set("displayHealth", yamlConfig.getBoolean("displayHealth", false));
			globalSection.set("keepDisplayHealthOn", yamlConfig.getBoolean("keepDisplayHealthOn", false));
			globalSection.set("displayHealthSymbol", yamlConfig.getString("displayHealthSymbol", "\u2764"));
			globalSection.set("displayHealthFormat", yamlConfig.getString("displayHealthFormat", "&b{hearts} &a/ &b{maxHearts} &c{displayHealthSymbol}"));
			
			// Port permissions
			ConfigurationSection permSectionOld = yamlConfig.getConfigurationSection("permissions");
			ConfigurationSection permSection = getInstance().getConfig().getConfigurationSection("permissions");
			for (String permName : permSectionOld.getKeys(false)) {
				permSection.set(permName, permSectionOld.get(permName));
			}
			
			// Port players
			ConfigurationSection playerSectionOld = yamlConfig.getConfigurationSection("players");
			ConfigurationSection playerSection = getInstance().getConfig().getConfigurationSection("players");
			for (String uuid : playerSectionOld.getKeys(false)) {
				playerSection.set(uuid + ".lastSeenAs", playerSectionOld.get(uuid + ".lastSeenAs", ""));
				playerSection.set(uuid + ".HP", playerSectionOld.get(uuid + ".HP", 20.0d));
				playerSection.set(uuid + ".extraHearts", playerSectionOld.get(uuid + ".extraHearts", 0));
			}
			
			// Only run when updating from v2.4.2 - v2.4.3.
			// Get options from old config and port to new config
			// Get players from old config and move to separate file for each	
		}
	}
	
	public void registerHealthBar() {
		if (this.scoreBoard.getObjective("health") != null) {
			this.scoreBoard.getObjective("health").unregister();
		}
		if (getConfiguration().isHealthbarsEnabled()) {
			Objective o = this.scoreBoard.registerNewObjective("health", "health");
			MoreHeartsUtil.o = o;
			if (getConfiguration().isHealthbarSymbolEnabled())
				o.setDisplayName(MoreHeartsUtil.replaceSymbols(getConfiguration().getHealthbarSymbol()));
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
				sum += instance.getConfig().getDouble("players." + player.getUniqueId() + ".extraHearts") * 2.0d;
			}
			else {
				instance.getConfig().set("players." + player.getUniqueId() + ".extraHearts", Integer.valueOf(0));
				instance.saveConfig();;
			}
			for (String str : getConfiguration().getPerms().keySet()) {
				if (player.isPermissionSet(str)) {
					sum += ((Double) getConfiguration().getPerms().get(str)).doubleValue() * 2.0d;
				}
			}
			player.setMaxHealth(sum);
			if (sum > getConfiguration().getDefaultHealth()) { // Has extra hearts - enable/disable hide hearts for that player
				player.setHealthScale(getConfiguration().getHideHeartAmount() * 2.0d);
				player.setHealthScaled(getConfiguration().isHideHeartsEnabled());
			}
			if (sum < getConfiguration().getHideHeartAmount()) {
				player.setHealthScale(sum);
			}
			if (!player.isDead()) {
				//player.setHealth(player.getMaxHealth());
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
		Collection<? extends Player> collectionOfPlayers = Bukkit.getOnlinePlayers();
		for (Player player : collectionOfPlayers) {
			refreshPlayer(player);
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
