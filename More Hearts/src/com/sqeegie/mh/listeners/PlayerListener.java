package com.sqeegie.mh.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.sqeegie.mh.DisplayHealth;
import com.sqeegie.mh.MoreHearts;
import com.sqeegie.mh.utils.MoreHeartsUtil;

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
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (!MoreHearts.getInstance().getConfig().contains("players." + player.getUniqueId())) {
			MoreHearts.getInstance().getConfig().set("players." + player.getUniqueId() + ".lastSeenAs", player.getName());
			MoreHearts.getInstance().getConfig().set("players." + player.getUniqueId() + ".HP", Double.valueOf(MoreHeartsUtil.roundToNthPlace(player.getHealth(), 2)));
			MoreHearts.getInstance().getConfig().set("players." + player.getUniqueId() + ".extraHearts", Integer.valueOf(0));
			MoreHearts.getInstance().saveConfig();
		}
		MoreHearts.refreshPlayer(player);
		DisplayHealth.sendUpdate(player);
		MoreHeartsUtil.refreshHealthbar();
		
		double savedHealth = MoreHearts.getInstance().getConfig().getDouble("players." + player.getUniqueId() + ".HP");
		if (player.getMaxHealth() >= savedHealth && player.getHealth() != savedHealth)
			player.setHealth(MoreHearts.getInstance().getConfig().getDouble("players." + player.getUniqueId() + ".HP")); // Set the player's health to what they had before quitting the server the last time
	}
	/** Saves player's health data upon quitting. */
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		MoreHearts.getInstance().getConfig().set("players." + player.getUniqueId() + ".lastSeenAs", player.getName());
		if (MoreHearts.getConfiguration().getWorlds().contains(player.getWorld().getName())) {
			MoreHearts.getInstance().getConfig().set("players." + player.getUniqueId() + ".HP", Double.valueOf(MoreHeartsUtil.roundToNthPlace(player.getHealth(), 2)));
		}
		MoreHearts.getInstance().saveConfig();
		DisplayHealth.removeRunnable(player);
		player.setMaxHealth(20.0D);
	}

	/** Saves player's health data upon being kicked. */
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerKick(PlayerKickEvent event) {
		Player player = event.getPlayer();
		MoreHearts.getInstance().getConfig().set("players." + player.getUniqueId() + ".lastSeenAs", player.getName());
		if (MoreHearts.getConfiguration().getWorlds().contains(player.getWorld().getName())) {
			MoreHearts.getInstance().getConfig().set("players." + player.getUniqueId() + ".HP", Double.valueOf(MoreHeartsUtil.roundToNthPlace(player.getHealth(), 2)));
		}
		MoreHearts.getInstance().saveConfig();
		DisplayHealth.removeRunnable(player);
		player.setMaxHealth(20.0D);
	}

	/**
	 * Upon teleport, double check More Hearts is enabled for the world, then
	 * save the player's current health.
	 */
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		final Player player = event.getPlayer();
		if (MoreHearts.getConfiguration().getWorlds().contains(player.getWorld().getName())) {
			MoreHearts.getInstance().getConfig().set("players." + player.getUniqueId() + ".HP", Double.valueOf(MoreHeartsUtil.roundToNthPlace(player.getHealth(), 2)));
			MoreHearts.getInstance().saveConfig();
		}
		MoreHearts.refreshPlayer(player);
		
		/* Why was this scheduler here? Is it to get a more accurate measurement?
		Bukkit.getScheduler().scheduleSyncDelayedTask(MoreHearts.getInstance(), new Runnable() {
			public void run() {
				MoreHearts.refreshPlayer(player);
			}
		}, 1L);
		*/ 
		DisplayHealth.sendUpdate(player);
		MoreHeartsUtil.refreshHealthbar();
	}

	/** Updates file upon a player regaining health. */
	@EventHandler(priority=EventPriority.MONITOR)
	public void onHealthRegain(EntityRegainHealthEvent event) { // TODO: Replace this with a /heal compatible listener.
		if ((event.getEntity() instanceof Player)) {
			Player player = (Player) event.getEntity();
			if (MoreHearts.getConfiguration().getWorlds().contains(player.getWorld().getName())) {
				MoreHearts.getInstance().getConfig().set("players." + player.getUniqueId() + ".HP", Double.valueOf(MoreHeartsUtil.roundToNthPlace(player.getHealth(), 2)));
				MoreHearts.getInstance().saveConfig();
			}
			MoreHeartsUtil.refreshHealthbar();
			DisplayHealth.sendUpdate(player);
		}
	}

	/** Updates file upon a player taking damage. */
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerDamage(EntityDamageEvent event) {
		if ((event.getEntity() instanceof Player)) {
			Player player = (Player) event.getEntity();
			if (MoreHearts.getConfiguration().getWorlds().contains(player.getWorld().getName())) {
				MoreHearts.getInstance().getConfig().set("players." + player.getUniqueId() + ".HP", Double.valueOf(MoreHeartsUtil.roundToNthPlace(player.getHealth(), 2)));
				MoreHearts.getInstance().saveConfig();
			}
			MoreHeartsUtil.refreshHealthbar();
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (MoreHearts.getConfiguration().getWorlds().contains(player.getWorld().getName())) {
            MoreHearts.getInstance().getConfig().set("players." + player.getUniqueId() + ".HP", Double.valueOf(MoreHeartsUtil.roundToNthPlace(player.getHealth(), 2)));
            MoreHearts.getInstance().saveConfig();
        }
        MoreHeartsUtil.refreshHealthbar();
        DisplayHealth.sendUpdate(player);
    }
    
    /** Attempted fix for /heal-esk commands. */
	@EventHandler
	public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.getMessage().contains("heal") || event.getMessage().contains("mend")) {
			if (event.getMessage().equals("/heal") || event.getMessage().equals("/mend") || event.getMessage().equals("/fullheal")) { // Heal command is being executed.
				// TODO: Finish implementing this
			}
			else if (event.getMessage().contains("/heal ") || event.getMessage().contains("/mend ") || event.getMessage().contains("/fullheal ")) { // Possible addition arguments
				// TODO: Add a better additional argument checker
			}
		}

	}
}
