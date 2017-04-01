package com.sqeegie.mh.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;

import com.sqeegie.mh.MoreHearts;

public class MoreHeartsUtil {
	public static Objective o;

	public static void refreshHealthbar() {
		if (MoreHearts.getInstance().getConfig().getBoolean("useHeartsAsHealthbarType")) { // TODO: Fix this... Not working for some reason.
			int health = o.getScore("health").getScore();
			o.getScore("health").setScore(MoreHeartsUtil.roundToNearest(health / 2));
		}
	}

	public static String replaceSymbols(String str) {
		return ChatColor.translateAlternateColorCodes('&', str);
	}

	public static int roundToNearest(double d) {
		double remainder = d - (int) d;
		if (remainder <= 0.5D) {
			return (int) d;
		}
		return (int) d + 1;
	}

	public static double roundToTenths(double d) {
		BigDecimal bd = new BigDecimal(d).setScale(1, RoundingMode.HALF_EVEN);
		d = bd.doubleValue();
		return d;
	}

	public static double roundToNthPlace(double d, int place) {
		BigDecimal bd = new BigDecimal(d).setScale(place, RoundingMode.HALF_EVEN);
		d = bd.doubleValue();
		return d;
	}

	public static Player getPlayerFromUUID(String uuid) {
		try {
			return getPlayerFromUUID(UUID.fromString(uuid));
		}
		catch (IllegalArgumentException e) {
			return null;
		}
	}

	public static Player getPlayerFromUUID(UUID uuid) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getUniqueId().equals(uuid)) {
				return player;
			}
		}
		return null;
	}
}
