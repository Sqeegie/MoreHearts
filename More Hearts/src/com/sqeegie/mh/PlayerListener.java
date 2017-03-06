package com.sqeegie.mh;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * 
 * @authors roei12, Sqeegie
 *
 */

public class PlayerListener implements Listener {
	private MoreHearts plug;

	public PlayerListener(MoreHearts pl) {
		plug = pl;
	}

	/** Updates player's health data upon joining. */
	@EventHandler
	public void pje(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		if (!MoreHearts.getConfiguration().contains("players." + player.getUniqueId())) {
			MoreHearts.getConfiguration().set("players." + player.getUniqueId() + ".lastSeenAs", player.getName());
			MoreHearts.getConfiguration().set("players." + player.getUniqueId() + ".HP", Double.valueOf(player.getHealth()));
			MoreHearts.getConfiguration().set("players." + player.getUniqueId() + ".extraHearts", Integer.valueOf(0));
			MoreHearts.saveConfiguration();
		}
		MoreHearts.refreshPlayer(player);
	}

	/** Saves player's health data upon quitting. */
	@EventHandler
	public void pqe(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		MoreHearts.getConfiguration().set("players." + player.getUniqueId() + ".lastSeenAs", player.getName());
		if (MoreHearts.getWorlds().contains(player.getWorld().getName())) {
			MoreHearts.getConfiguration().set("players." + player.getUniqueId() + ".HP", Double.valueOf(player.getHealth()));
		}
		MoreHearts.saveConfiguration();
		player.setMaxHealth(20.0D);
	}

	/** Saves player's health data upon being kicked. */
	@EventHandler
	public void pke(PlayerKickEvent e) {
		Player player = e.getPlayer();
		MoreHearts.getConfiguration().set("players." + player.getUniqueId() + ".lastSeenAs", player.getName());
		if (MoreHearts.getWorlds().contains(player.getWorld().getName())) {
			MoreHearts.getConfiguration().set("players." + player.getUniqueId() + ".HP", Double.valueOf(player.getHealth()));
		}
		MoreHearts.saveConfiguration();
		player.setMaxHealth(20.0D);
	}

	/**
	 * Upon teleport, double check More Hearts is enabled for the world, then
	 * save the player's current health.
	 */
	@EventHandler
	public void pte(PlayerTeleportEvent e) {
		final Player player = e.getPlayer();
		if (MoreHearts.getWorlds().contains(player.getWorld().getName())) {
			MoreHearts.getConfiguration().set("players." + player.getUniqueId() + ".HP", Double.valueOf(player.getHealth()));
			MoreHearts.saveConfiguration();
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(MoreHearts.getInstance(), new Runnable() {
			public void run() {
				MoreHearts.refreshPlayer(player);
			}
		}, 1L);
	}

	/** Updates file upon a player regaining health. */
	@EventHandler
	public void hre(EntityRegainHealthEvent e) { // TODO: Replace this with a /heal compatible listener.
		if ((e.getEntity() instanceof Player)) {
			Player player = (Player) e.getEntity();
			if (MoreHearts.getWorlds().contains(player.getWorld().getName())) {
				MoreHearts.getConfiguration().set("players." + player.getUniqueId() + ".HP", Double.valueOf(player.getHealth()));
				MoreHearts.saveConfiguration();
			}
		}
	}

	/** Updates file upon a player taking damage. */
	@EventHandler
	public void pde(EntityDamageEvent e) {
		if ((e.getEntity() instanceof Player)) {
			Player player = (Player) e.getEntity();
			if (MoreHearts.getWorlds().contains(player.getWorld().getName())) {
				MoreHearts.getConfiguration().set("players." + player.getUniqueId() + ".HP", Double.valueOf(player.getHealth()));
				MoreHearts.saveConfiguration();
			}
		}
	}
	
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {
		if (e.getMessage().contains("heal") || e.getMessage().contains("mend")) {
			if (e.getMessage().equals("/heal") || e.getMessage().equals("/mend") || e.getMessage().equals("/fullheal")) { // Heal command is being executed.
				// TODO: Finish implementing this
			}
			else if (e.getMessage().contains("/heal ") || e.getMessage().contains("/mend ") || e.getMessage().contains("/fullheal ")) { // Possible addition arguments
				// TODO: Add a better additonal argument checker
			}
		}

	}
}
