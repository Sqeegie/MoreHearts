package com.sqeegie.mh;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.minecraft.server.v1_12_R1.ChatMessageType;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;

public class DisplayNew { // Quick and dirty fix (aka. Copy someone else's work)
	public DisplayNew(Player player, String message) {
		IChatBaseComponent cbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + message + "\"}");
		PacketPlayOutChat  packetPlayOutChat = new PacketPlayOutChat(cbc, ChatMessageType.GAME_INFO);
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(packetPlayOutChat);
		
		if (MoreHearts.getConfiguration().keepDisplayHealthOn()) {
			if (Bukkit.getOnlinePlayers().contains(player)) {
				if (!DisplayHealth.runnables.contains(player)) {
					DisplayHealth.runnables.add(player);
				}
				
				new BukkitRunnable() {
					public void run() {
						DisplayHealth.sendDisplayHealth(player, MoreHearts.getConfiguration().getDisplayHealthFormat());
					}
				}.runTaskLater(MoreHearts.getInstance(), 10L); // Aprox. 250ms refresh
			}
		}
	}
}
