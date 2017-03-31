package com.sqeegie.mh;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.sqeegie.mh.utils.MoreHeartsUtil;

public class DisplayHealth {
	private static ArrayList<Player> runnables = new ArrayList<Player>();

	public static void removeRunnable(Player player) {
		if (runnables.contains(player)) {
			runnables.remove(player);
		}
	}

	public static void sendUpdate(Player player) {
		if (MoreHearts.getInstance().getConfig().getBoolean("displayHealth")) { // If display health is enabled
			if (MoreHearts.getInstance().getConfig().getBoolean("keepDisplayHealthOn")) {
				if (!runnables.contains(player)) {
					new BukkitRunnable() {
						public void run() {
							sendDisplayHealth(player, MoreHearts.getInstance().getConfig().getString("displayHealthFormat"));
						}
					}.runTaskLater(MoreHearts.getInstance(), 1L);
				}
			}
			else {
				new BukkitRunnable() {
					public void run() {
						sendDisplayHealth(player, MoreHearts.getInstance().getConfig().getString("displayHealthFormat"));
					}
				}.runTaskLater(MoreHearts.getInstance(), 1L);
			}
		}
	}

	private static void sendDisplayHealth(Player player, String message) {
		// Formatting
		String displayHealthSymbol = MoreHearts.getInstance().getConfig().getString("displayHealthSymbol");
		Integer code = Integer.parseInt(displayHealthSymbol.substring(2), 16);
		char character = Character.toChars(code)[0];
		
		double maxHealth = MoreHearts.getInstance().getConfig().getDouble("defaultHearts") * 2.0d;
		if (MoreHearts.getInstance().getConfig().contains("players." + player.getUniqueId() + ".extraHearts")) {
			if (MoreHearts.getInstance().getConfig().getDouble("players." + player.getUniqueId() + ".extraHearts") != 0) {
				maxHealth += MoreHearts.getInstance().getConfig().getDouble("players." + player.getUniqueId() + ".extraHearts") * 2.0d;	
			}
		}

		message = ChatColor.translateAlternateColorCodes('&', message).replace("{hearts}", String.valueOf(MoreHeartsUtil.roundToTenths(player.getHealth() / 2))).replace("{health}", String.valueOf(MoreHeartsUtil.roundToNearest(player.getHealth()))).replace("{maxHealth}", String.valueOf(MoreHeartsUtil.roundToNearest(maxHealth))).replace("{maxHearts}", String.valueOf(MoreHeartsUtil.roundToTenths(maxHealth / 2))).replace("{displayHealthSymbol}", Character.toString(character));
		
		//  Get server version
		final String SERVER_VERSION;
		String mcVersion = Bukkit.getServer().getClass().getPackage().getName();
		mcVersion = mcVersion.substring(mcVersion.lastIndexOf(".") + 1);
		SERVER_VERSION = mcVersion;

		final boolean useOldMethods = SERVER_VERSION.equalsIgnoreCase("v1_8_R1") || SERVER_VERSION.equalsIgnoreCase("v1_7_");

		// Send packet
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
				if (Bukkit.getOnlinePlayers().contains(player)) {
					if (!runnables.contains(player)) {
						runnables.add(player);
					}
					
					new BukkitRunnable() {
						public void run() {
							sendDisplayHealth(player, MoreHearts.getInstance().getConfig().getString("displayHealthFormat"));
						}
					}.runTaskLater(MoreHearts.getInstance(), 10L); // Aprox. 250ms refresh
				}
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
