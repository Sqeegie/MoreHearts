package com.sqeegie.mh.listeners;

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
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.sqeegie.mh.MoreHearts;

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
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
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
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		MoreHearts.getConfiguration().set("players." + player.getUniqueId() + ".lastSeenAs", player.getName());
		if (MoreHearts.getWorlds().contains(player.getWorld().getName())) {
			MoreHearts.getConfiguration().set("players." + player.getUniqueId() + ".HP", Double.valueOf(player.getHealth()));
		}
		MoreHearts.saveConfiguration();
		player.setMaxHealth(20.0D);
	}

	/** Saves player's health data upon being kicked. */
	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		Player player = event.getPlayer();
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
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		final Player player = event.getPlayer();
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
	public void onHealthRegain(EntityRegainHealthEvent event) { // TODO: Replace this with a /heal compatible listener.
		if ((event.getEntity() instanceof Player)) {
			Player player = (Player) event.getEntity();
			if (MoreHearts.getWorlds().contains(player.getWorld().getName())) {
				MoreHearts.getConfiguration().set("players." + player.getUniqueId() + ".HP", Double.valueOf(player.getHealth()));
				MoreHearts.saveConfiguration();
			}
		}
	}

	/** Updates file upon a player taking damage. */
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event) {
		if ((event.getEntity() instanceof Player)) {
			Player player = (Player) event.getEntity();
			if (MoreHearts.getWorlds().contains(player.getWorld().getName())) {
				MoreHearts.getConfiguration().set("players." + player.getUniqueId() + ".HP", Double.valueOf(player.getHealth()));
				MoreHearts.saveConfiguration();
			}
		}
	}
	
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (MoreHearts.getWorlds().contains(player.getWorld().getName())) {
            MoreHearts.getConfiguration().set("Players." + player.getUniqueId() + ".HP", Double.valueOf(player.getHealth()));
            MoreHearts.saveConfiguration();
        }
    }
    
	@EventHandler
	public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.getMessage().contains("heal") || event.getMessage().contains("mend")) {
			if (event.getMessage().equals("/heal") || event.getMessage().equals("/mend") || event.getMessage().equals("/fullheal")) { // Heal command is being executed.
				// TODO: Finish implementing this
			}
			else if (event.getMessage().contains("/heal ") || event.getMessage().contains("/mend ") || event.getMessage().contains("/fullheal ")) { // Possible addition arguments
				// TODO: Add a better additonal argument checker
			}
		}

	}
}
