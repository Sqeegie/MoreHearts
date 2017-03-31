package com.sqeegie.mh.utils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
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
	
	public static double roundToNearestHalfTenths(double d) {
		d = roundToNthPlace(d, 2);
		String dub = String.valueOf(d);
		double lastDigits = Double.valueOf(dub.substring(dub.length() - 2));
		if (lastDigits < 25) {
			// Round down
			dub = dub.replace(String.valueOf(lastDigits), "0");
			d = Double.valueOf(dub);
		}
		else if (lastDigits > 25 && lastDigits < 50) {
			// Round to .50
			dub = dub.replace(String.valueOf(lastDigits), "5");
			d = Double.valueOf(dub);
		}
		else if (lastDigits == 50) {
			// Stay the same
			d = Double.valueOf(dub);
		}
		else if (lastDigits > 50 && lastDigits < 75) {
			// Round to .50
			dub = dub.replace(String.valueOf(lastDigits), "5");
			d = Double.valueOf(dub);
		}
		else if (lastDigits > 75) {
			// Round up
			char[] firstDigit = Character.toChars((int) lastDigits);
			dub = dub.replace(String.valueOf(lastDigits), String.valueOf(firstDigit[0]));
			d = Double.valueOf(firstDigit[0]);
		}
		return d;
	}
	
	public static double roundToNthPlace(double d, int place) {
		BigDecimal bd = new BigDecimal(d).setScale(place, RoundingMode.HALF_EVEN);
		d = bd.doubleValue();
		return d;
	}

	public static FileConfiguration loadConfig(String path, MoreHearts instance) {
		if (!path.endsWith(".yml")) {
			path = path + ".yml";
		}
		File file = new File(instance.getDataFolder(), path);
		if (!file.exists()) {
			try {
				//instance.saveResource(path, false);
				instance.saveDefaultConfig();
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
}
