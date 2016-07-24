package com.sqeegie.mh;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener {
	private MoreHearts plugin;
  
	public PlayerListener(MoreHearts pl) {
    	plugin = pl;
	}
  
	@EventHandler
	public void pje(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		if (!plugin.getConfig().contains("Players." + player.getUniqueId())) {
			plugin.getConfig().set("Players." + player.getUniqueId() + ".LastSeenAs", player.getName());
			plugin.getConfig().set("Players." + player.getUniqueId() + ".HP", Double.valueOf(player.getHealth()));
			plugin.getConfig().set("Players." + player.getUniqueId() + ".ExtraHearts", Integer.valueOf(0));
			plugin.saveConfig();
		}
		plugin.refreshPlayer(player);
	}
  
	@EventHandler
	public void pqe(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		plugin.getConfig().set("Players." + player.getUniqueId() + ".LastSeenAs", player.getName());
		if (plugin.worlds.contains(player.getWorld().getName())) {
			plugin.getConfig().set("Players." + player.getUniqueId() + ".HP", Double.valueOf(player.getHealth()));
		}
		plugin.saveConfig();
		player.setMaxHealth(20.0D);
	}
  
	@EventHandler
	public void pke(PlayerKickEvent e) {
		Player player = e.getPlayer();
		plugin.getConfig().set("Players." + player.getUniqueId() + ".LastSeenAs", player.getName());
		if (plugin.worlds.contains(player.getWorld().getName())) {
			plugin.getConfig().set("Players." + player.getUniqueId() + ".HP", Double.valueOf(player.getHealth()));
		}
		plugin.saveConfig();
		player.setMaxHealth(20.0D);
	}
  
	@EventHandler
	public void pte(PlayerTeleportEvent e) {
		final Player player = e.getPlayer();
		if (plugin.worlds.contains(player.getWorld().getName())) {
			plugin.getConfig().set("Players." + player.getUniqueId() + ".HP", Double.valueOf(player.getHealth()));
			plugin.saveConfig();
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				plugin.refreshPlayer(player);
			}
		}, 1L);
	}
  
	@EventHandler
	public void hre(EntityRegainHealthEvent e) {
		if ((e.getEntity() instanceof Player)) {
			Player player = (Player)e.getEntity();
			if (plugin.worlds.contains(player.getWorld().getName()))
			{
				plugin.getConfig().set("Players." + player.getUniqueId() + ".HP", Double.valueOf(player.getHealth()));
				plugin.saveConfig();
			}
		}
	}	
  
	@EventHandler
	public void pde(EntityDamageEvent e) {
		if ((e.getEntity() instanceof Player)) {
			Player player = (Player)e.getEntity();
			if (plugin.worlds.contains(player.getWorld().getName())) {
				plugin.getConfig().set("Players." + player.getUniqueId() + ".HP", Double.valueOf(player.getHealth()));
				plugin.saveConfig();
			}
		}
	}
}