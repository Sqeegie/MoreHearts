package com.sqeegie.mh;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.sqeegie.mh.utils.MoreHeartsUtil;

import net.minecraft.server.v1_12_R1.ChatMessageType;

public class DisplayHealth {
	public static ArrayList<Player> runnables = new ArrayList<Player>();

	public static void removeRunnable(Player player) {
		if (runnables.contains(player)) {
			runnables.remove(player);
		}
	}

	public static void sendUpdate(Player player) {
		if (MoreHearts.getConfiguration().isDisplayHealthEnabled()) { // If display health is enabled
			if (MoreHearts.getConfiguration().keepDisplayHealthOn()) {
				if (!runnables.contains(player)) {
					new BukkitRunnable() {
						public void run() {
							sendDisplayHealth(player, MoreHearts.getConfiguration().getDisplayHealthFormat());
						}
					}.runTaskLater(MoreHearts.getInstance(), 1L);
				}
			}
			else {
				new BukkitRunnable() {
					public void run() {
						sendDisplayHealth(player, MoreHearts.getConfiguration().getDisplayHealthFormat());
					}
				}.runTaskLater(MoreHearts.getInstance(), 1L);
			}
		}
	}

	public static void sendDisplayHealth(Player player, String message) {
		// Formatting
		String displayHealthSymbol = MoreHearts.getConfiguration().getDisplayHealthSymbol();
		Integer code = Integer.parseInt(displayHealthSymbol.substring(2), 16);
		char character = Character.toChars(code)[0];
		
		double maxHealth = MoreHearts.getConfiguration().getDefaultHealth();
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

		final boolean useAncientMethods = SERVER_VERSION.equalsIgnoreCase("v1_8_R1") || SERVER_VERSION.contains("v1_7_");
		
		// Send packet
		try {
			
			
			Class<?> c1 = Class.forName("org.bukkit.craftbukkit." + SERVER_VERSION + ".entity.CraftPlayer");
			Object p = c1.cast(player);
			Object ppoc;
			Class<?> c4 = Class.forName("net.minecraft.server." + SERVER_VERSION + ".PacketPlayOutChat");
			Class<?> c5 = Class.forName("net.minecraft.server." + SERVER_VERSION + ".Packet");
			
			if (SERVER_VERSION.contains("v1_8_") || SERVER_VERSION.contains("v1_7_")) { // 1.7, 1.8
				Class<?> c2 = Class.forName("net.minecraft.server." + SERVER_VERSION + ".ChatSerializer");
				Class<?> c3 = Class.forName("net.minecraft.server." + SERVER_VERSION + ".IChatBaseComponent");
				Method m3 = c2.getDeclaredMethod("a", String.class);
				Object cbc = c3.cast(m3.invoke(c2, "{\"text\": \"" + message + "\"}"));
				ppoc = c4.getConstructor(new Class<?>[] { c3, byte.class }).newInstance(cbc, (byte) 2);
			}
			else if (SERVER_VERSION.contains("v1_9_") || SERVER_VERSION.contains("v1_10_") || SERVER_VERSION.contains("v1_11_")) { // 1.9, 1.10, 1.11
				Class<?> c2 = Class.forName("net.minecraft.server." + SERVER_VERSION + ".ChatComponentText");
				Class<?> c3 = Class.forName("net.minecraft.server." + SERVER_VERSION + ".IChatBaseComponent");
				Object o = c2.getConstructor(new Class<?>[] { String.class }).newInstance(message);
				ppoc = c4.getConstructor(new Class<?>[] { c3, byte.class }).newInstance(o, (byte) 2);
			}
			else { // 1.12
				new DisplayNew(player, message); // Quick fix
				return;
			}

			Method m1 = c1.getDeclaredMethod("getHandle");
			Object h = m1.invoke(p);
			Field f1 = h.getClass().getDeclaredField("playerConnection");
			Object pc = f1.get(h);
			Method m5 = pc.getClass().getDeclaredMethod("sendPacket", c5);
			m5.invoke(pc, ppoc);
			
			if (MoreHearts.getConfiguration().keepDisplayHealthOn()) {
				if (Bukkit.getOnlinePlayers().contains(player)) {
					if (!runnables.contains(player)) {
						runnables.add(player);
					}
					
					new BukkitRunnable() {
						public void run() {
							sendDisplayHealth(player, MoreHearts.getConfiguration().getDisplayHealthFormat());
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
