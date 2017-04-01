package com.sqeegie.mh.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.sqeegie.mh.MoreHearts;
import com.sqeegie.mh.utils.MoreHeartsUtil;

public class MoreHeartsConfiguration {
	public static final int CURRENT_CONFIG_VERSION = 1;
    public static final String DESTINATION_FILE_NAME = "config.yml";
    public static final String CLASSPATH_RESOURCE_NAME = "/config.yml";

	private int defaultHealth;
	private int maxHearts;
	private int resetPassword;
	private boolean hideHearts;
	private double hideHeartAmount;
	private boolean enableHealthbars;
	private boolean heartsAsHealthbarType;
	private String healthbarSymbol;
	private boolean enableHealthbarSymbol;
	private boolean displayHealth;
	private boolean keepDisplayHealthOn;
	private String displayHealthSymbol;
	private String displayHealthFormat;

	private HashMap<String, MoreHeartsPlayer> playerStats;
	private HashMap<String, Double> perms = new HashMap<String, Double>();
	private ArrayList<String> worlds = new ArrayList<String>();

	public MoreHeartsConfiguration(Configuration config) {
		loadByConfiguration(config);
	}

	public void loadByConfiguration(Configuration config) {

		ConfigurationSection globalSection = config.getConfigurationSection("global");
		defaultHealth = globalSection.getInt("defaultHearts", 10) * 2;
		maxHearts = globalSection.getInt("maxHearts", 250);
		resetPassword = globalSection.getInt("resetPassword", 465004);
		hideHearts = globalSection.getBoolean("hideHearts", false);
		hideHeartAmount = globalSection.getDouble("hideHeartsDisplayAmount", 10);
		enableHealthbars = globalSection.getBoolean("enablePlayerHealthbars", false);
		heartsAsHealthbarType = globalSection.getBoolean("useHeartsAsHealthbarType", true);
		healthbarSymbol = globalSection.getString("healthbarSymbol", " &c❤");
		enableHealthbarSymbol = globalSection.getBoolean("enableHealthbarSymbol", true);
		displayHealth = globalSection.getBoolean("displayHealth", false);
		keepDisplayHealthOn = globalSection.getBoolean("keepDisplayHealthOn", false);
		displayHealthSymbol = globalSection.getString("displayHealthSymbol", "\u2764");
		displayHealthFormat = globalSection.getString("displayHealthFormat", "&b{hearts} &a/ &b{maxHearts} &c{displayHealthSymbol}");



		ConfigurationSection permSection = config.getConfigurationSection("permissions");
		for (String str : permSection.getKeys(false)) {
			perms.put("morehearts." + str, Double.valueOf(permSection.getDouble("permissions." + str) * 2.0D));
		}

		/*
		ConfigurationSection playerSection = config.getConfigurationSection("players");
		for (String uuid : playerSection.getKeys(false)) {
			Player player = MoreHeartsUtil.getPlayerFromUUID(uuid);
			MoreHeartsPlayer mhPlayer = new MoreHeartsPlayer(uuid, player, MoreHeartsUtil.roundToTenths(player.getHealth()), Double extraHearts);
			playerStats.put(player.getName(), mhPlayer);
		}
		*/
		
		config.getInt("config-version", 0);
		
	}

	public ArrayList<String> parseWorlds(String ws) {
		if (ws != null) {
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
		return worlds;
	}

	public void saveWorlds() {
		String ws = (String) worlds.get(0);
		for (int a = 1; a < worlds.size(); a++) {
			ws = ws + "," + (String) worlds.get(a);
		}
		MoreHearts.getInstance().getConfig().set("enabledIn", ws);
		MoreHearts.getInstance().saveConfig();
	}

	public void refreshWorlds() {
		worlds.clear();
		ConfigurationSection globalSection = MoreHearts.getInstance().getConfig().getConfigurationSection("global");
		String enabledIn = globalSection.getString("enabledIn", "world,world_nether,world_the_end");
		worlds = parseWorlds(enabledIn);
	}
	
	public ArrayList<String> getWorlds() {
		return worlds;
	}

	public void addWorld(String world) {
		worlds.add(world);
	}

	public void removeWorld(String world) {
		worlds.remove(world);
		saveWorlds();
	}
	
	public void refreshPerms() {
        perms.clear();
        for (String str : MoreHearts.getInstance().getConfig().getConfigurationSection("permissions").getKeys(false)) {
            perms.put("morehearts." + str, Double.valueOf(MoreHearts.getInstance().getConfig().getDouble("permissions." + str) * 2.0D));
        }
	}
	
	public HashMap<String, Double> getPerms() {
		return perms;
	}
	
	public int getDefaultHealth() {
		return defaultHealth;
	}
	
	public double getHideHeartAmount() {
		return hideHeartAmount;
	}
	
	public boolean isHideHeartsOn() {
		return hideHearts;
	}
}
