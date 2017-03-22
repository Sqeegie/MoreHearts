package com.sqeegie.mh.utils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.sqeegie.mh.MoreHearts;

public class MoreHeartsUtil {

	public static String replaceSymbols(String str) {
		return ChatColor.translateAlternateColorCodes('&', str);
	}

	public static String formatDisplayFor(Player player, String str) { // TODO: Fix this.... For some reason it's not replacing.
		String output = str;
		output.replaceAll("{health}", String.valueOf(player.getHealth()));
		output.replaceAll("{hearts}", String.valueOf((player.getHealth() / 2)));

		if (MoreHearts.getInstance().getConfig().contains("players." + player.getUniqueId() + ".extraHearts")) {
			double extraHearts = MoreHearts.getInstance().getConfig().getDouble("players." + player.getUniqueId() + ".extraHearts");
			output.replaceAll("{maxHealth}", String.valueOf((extraHearts * 2.0d)));
			output.replaceAll("{maxHearts}", String.valueOf((extraHearts)));
		}

		String displayHealthSymbol = MoreHearts.getInstance().getConfig().getString("displayHealthSymbol");

		// TODO: May need to do something about parsing unicode from the config.

		output.replaceAll("{displayHealthSymbol}", displayHealthSymbol);

		return ChatColor.translateAlternateColorCodes('&', output);
	}

	public static void sendUpdatedHealthIfEnabled(Player player) { // TODO: Implement the "stay on" feature for display health.
		if (MoreHearts.getInstance().getConfig().getBoolean("displayHealth")) {
			sendDisplayHealth(player, formatDisplayFor(player, MoreHearts.getInstance().getConfig().getString("displayHealthFormat")));
		}
	}

	public static int round(double d) {
		double remainder = d - (int) d;
		if (remainder <= 0.5D) {
			return (int) d;
		}
		return (int) d + 1;
	}

	public static FileConfiguration loadConfig(String path, MoreHearts instance) {
		if (!path.endsWith(".yml")) {
			path = path + ".yml";
		}
		File file = new File(instance.getDataFolder(), path);
		if (!file.exists()) {
			try {
				instance.saveResource(path, false);
			}
			catch (Exception e) {
				e.printStackTrace();
				System.out.println("-------------------------------------------------");
				System.out.println("[MoreHearts] Cannot save " + path + " to disk!");
				System.out.println("-------------------------------------------------");
				return null;
			}
		}
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		return config;
	}

	private static void sendDisplayHealth(Player player, String message) {
		final String SERVER_VERSION;
		String mcVersion = Bukkit.getServer().getClass().getPackage().getName();
		mcVersion = mcVersion.substring(mcVersion.lastIndexOf(".") + 1);
		SERVER_VERSION = mcVersion;

		final boolean useOldMethods = SERVER_VERSION.equalsIgnoreCase("v1_8_R1") || SERVER_VERSION.equalsIgnoreCase("v1_7_");

		try {
			Class<?> c1 = Class.forName("org.bukkit.craftbukkit." + SERVER_VERSION + ".entity.CraftPlayer");
			Object p = c1.cast(player);
			Object ppoc;
			Class<?> c4 = Class.forName("net.minecraft.server." + SERVER_VERSION + ".PacketPlayOutChat");
			Class<?> c5 = Class.forName("net.minecraft.server." + SERVER_VERSION + ".Packet");

			if (useOldMethods) {
				Class<?> c2 = Class.forName("net.minecraft.server." + SERVER_VERSION + ".ChatSerializer");
				Class<?> c3 = Class.forName("net.minecraft.server." + SERVER_VERSION + ".IChatBaseComponent");
				Method m3 = c2.getDeclaredMethod("a", String.class);
				Object cbc = c3.cast(m3.invoke(c2, "{\"text\": \"" + message + "\"}"));
				ppoc = c4.getConstructor(new Class<?>[] { c3, byte.class }).newInstance(cbc, (byte) 2);
			}
			else {
				Class<?> c2 = Class.forName("net.minecraft.server." + SERVER_VERSION + ".ChatComponentText");
				Class<?> c3 = Class.forName("net.minecraft.server." + SERVER_VERSION + ".IChatBaseComponent");
				Object o = c2.getConstructor(new Class<?>[] { String.class }).newInstance(message);
				ppoc = c4.getConstructor(new Class<?>[] { c3, byte.class }).newInstance(o, (byte) 2);
			}

			Method m1 = c1.getDeclaredMethod("getHandle");
			Object h = m1.invoke(p);
			Field f1 = h.getClass().getDeclaredField("playerConnection");
			Object pc = f1.get(h);
			Method m5 = pc.getClass().getDeclaredMethod("sendPacket", c5);
			m5.invoke(pc, ppoc);

			if (MoreHearts.getInstance().getConfig().getBoolean("keepDisplayHealthOn")) {
				new BukkitRunnable() {
					public void run() {
						sendDisplayHealth(player, message);
					}
				}.runTaskLater(MoreHearts.getInstance(), 4L);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
