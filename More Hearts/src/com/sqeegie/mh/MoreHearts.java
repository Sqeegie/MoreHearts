package com.sqeegie.mh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
public class MoreHearts extends JavaPlugin {
	
	Logger logger = Logger.getLogger("minecraft");
	HashMap<String, Double> perms = new HashMap();
	ArrayList<String> worlds = new ArrayList();
	String noPerm = ChatColor.RED + "You don't have permission to use this command";
	double defaultHearts = 20.0D;
	boolean vanishHearts = false;
	  
	public void onEnable() {
		logger.info("[MoreHearts] MoreHearts has been enabled!");
	    
	    Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
	    
	    // Create default config if non-existent
	    if (!getConfig().contains("Players")) {
	    	int rp = 1000 + (int)(Math.random() * 999999.0D);
	    	getConfig().createSection("Players");
	    	getConfig().createSection("Permissions");
	    	getConfig().set("DefaultHearts", Integer.valueOf(10));
	    	getConfig().set("EnableIn", ((World)Bukkit.getWorlds().get(0)).getName());
	    	getConfig().set("HideHearts", Boolean.valueOf(false));
	    	getConfig().set("ResetPassword", Integer.valueOf(rp));
	    	saveConfig();
	    }
	    
	    defaultHearts = (getConfig().getDouble("DefaultHearts") * 2.0D);
	    vanishHearts = getConfig().getBoolean("HideHearts");
	    
	    refreshPerms();
	    refreshWorlds();
	    
	    Player[] arrayOfPlayers = Bukkit._INVALID_getOnlinePlayers();
	    int numOfPlayers = arrayOfPlayers.length;
	    for (int i = 0; i < numOfPlayers; i++) {
	    	Player player = arrayOfPlayers[i];
	    	refreshPlayer(player);
	    }
	  }
	  
	public void onDisable() {
		logger.info("[MoreHearts] MoreHearts has been disabled!");
	    
	    // Reset everybody's health back to MC's default
	    Player[] arrayOfPlayers = Bukkit._INVALID_getOnlinePlayers();
	    int numOfPlayers = arrayOfPlayers.length;
	    for (int i = 0; i < numOfPlayers; i++) {
	    	Player player = arrayOfPlayers[i];
	    	
	    	getConfig().set("Players." + player.getUniqueId() + ".LastSeenAs", player.getName());
	    	getConfig().set("Players." + player.getUniqueId() + ".HP", Double.valueOf(player.getHealth()));
	    	player.setMaxHealth(20.0D);
	    }
	    
	    // Save to config which worlds the plugin is enabled in
	    String ws = (String)worlds.get(0);
	    for (int i = 1; i < worlds.size(); i++) {
	      ws = ws + "," + (String)worlds.get(i);
	    }
	    getConfig().set("EnableIn", ws);
	    saveConfig();
	}
	  
	public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {

		if (command.equalsIgnoreCase("hearts")) {
			if (sender instanceof Player) {
				if (sender.hasPermission("morehearts.hearts")) {
					Player player = getPlayerByUsername(sender.getName());
					sender.sendMessage(ChatColor.AQUA + "You have " + ChatColor.GREEN + player.getHealth() / 2.0D + ChatColor.AQUA + "/" + ChatColor.GREEN + player.getMaxHealth() / 2.0D + ChatColor.AQUA + " hearts!");
				}
				else {
					sender.sendMessage(noPerm);
					return false;
				}
			}
		}
		if (command.equalsIgnoreCase("mh") || command.equalsIgnoreCase("morehearts")) {
			if (args.length == 0) {
		    	if (sender.hasPermission("morehearts.help")) {
			        sendMessages(sender, ChatColor.GREEN + "MoreHearts help:" + 
			        ChatColor.AQUA + "<> - Required " + ChatColor.GREEN + "[] - Optional;" + 
			        ChatColor.AQUA + "/mh refresh -" + ChatColor.GREEN + " reload config;" + 
			        ChatColor.AQUA + "/mh add <player> <amount> - " + ChatColor.GREEN + "Add hearts to a player;" + 
			        ChatColor.AQUA + "/mh set <player> <amount> - " + ChatColor.GREEN + "Set hearts for a player;" + 
			        ChatColor.AQUA + "/mh addworld [world] - " + ChatColor.GREEN + "Enable MoreHearts in a world;" + 
			        ChatColor.AQUA + "/mh addallworlds - " + ChatColor.GREEN + "Will enable morehearts in every loaded world;" + 
			        ChatColor.AQUA + "/mh removeworld [world] - " + ChatColor.GREEN + "Remove a world;" + 
			        ChatColor.AQUA + "/mh worlds - " + ChatColor.GREEN + "All world that MoreHearts is enabled in;" + 
			        ChatColor.AQUA + "/mh reset <Password> - " + ChatColor.GREEN + "Delete config;" + 
			        ChatColor.AQUA + "/hearts - " + ChatColor.GREEN + "Check your real health (useful only if HideHearts is off");
		        }
		    	else {
		    		sender.sendMessage(noPerm);
		    		return false;
		    	}
			}
			else {
				if (args.length == 1) {
					
					if (!sender.hasPermission("morehearts." + args[0].toLowerCase())) {
						sender.sendMessage(noPerm);
						return false;
					}
					
					String str;
					if (args[0].equalsIgnoreCase("worlds")) {
						boolean b = true;
						sender.sendMessage(ChatColor.GREEN + "MoreHearts is enabled in:");
						for (Iterator localIterator = worlds.iterator(); localIterator.hasNext();) {
							str = (String)localIterator.next();
							if (b) {
								sender.sendMessage(ChatColor.AQUA + "- " + str);
							} 
							else {
								sender.sendMessage(ChatColor.GREEN + "- " + str);
							}
							b = !b;
						}
					}
					else if (args[0].equalsIgnoreCase("refresh")) {
						reloadConfig();
						refreshPerms();
						refreshWorlds();
						refreshAllPlayers();
						sender.sendMessage("Everything has been refreshed!");
					}
					else {
						if (args[0].equalsIgnoreCase("addworld")) {
							if ((sender instanceof Player)) {
								Player player = getPlayerByUsername(sender.getName());
								if (!worlds.contains(player.getWorld().getName())) {
									worlds.add(player.getWorld().getName());
									player.sendMessage(ChatColor.GREEN + "The world '" + ChatColor.GREEN + player.getWorld().getName() + ChatColor.AQUA + "' has been added!");
									
									refreshAllPlayers();
									saveWorlds();
								}
								else {
									player.sendMessage(ChatColor.RED + "MoreHearts is already enabled in this world");
								}
							}
						}
						else if (args[0].equalsIgnoreCase("removeworld")) {
							if ((sender instanceof Player)) {
								Player player = getPlayerByUsername(sender.getName());
								
								if (worlds.contains(player.getWorld().getName())) {
									worlds.remove(player.getWorld().getName());
									player.sendMessage(ChatColor.GREEN + "MoreHearts is no longer enabled in the world '" + ChatColor.GREEN + player.getWorld().getName() + ChatColor.AQUA + "'");
									
									refreshAllPlayers();
									saveWorlds();
								}
								else {
									player.sendMessage(ChatColor.RED + "MoreHearts is not enabled in this world");
								}
							}
						}
						else if (args[0].equalsIgnoreCase("addallworlds")) {
							for (World w : Bukkit.getWorlds()) {
								if (!worlds.contains(w.getName())) {
									worlds.add(w.getName());
								}
								saveWorlds();
							}
							refreshAllPlayers();
							sender.sendMessage(ChatColor.GREEN + "MoreHearts has been enabled in every loaded world!");
						}
						else if (args[0].equalsIgnoreCase("reset")) {
							sender.sendMessage(ChatColor.RED + "Usage: /mh reset <Password>");
						}
						else if (args[0].equalsIgnoreCase("add")) {
							sender.sendMessage(ChatColor.RED + "Usage: /mh add <Player> <Amount>");
						}
						else if (args[0].equalsIgnoreCase("set")) {
							sender.sendMessage(ChatColor.RED + "Usage: /mh set <Player> <Amount>");
						}
						else {
							sender.sendMessage(ChatColor.RED + "Unknown command.");
						}
					}
				}
				else if (args.length == 2) {
					if (!sender.hasPermission("morehearts." + args[0].toLowerCase())) {
						sender.sendMessage(noPerm);
					} 
					else if (args[0].equalsIgnoreCase("refresh")) {
						sender.sendMessage(ChatColor.RED + "Usage: /mh refresh");
					} 
					else if (args[0].equalsIgnoreCase("add")) {
						sender.sendMessage(ChatColor.RED + "Usage: /mh add <Player> <amount>");
					} 
					else if (args[0].equalsIgnoreCase("set")) {
						sender.sendMessage(ChatColor.RED + "Usage: /mh set <Player <Amount>");
					} 
					else if (args[0].equalsIgnoreCase("addworld")) {
						if (!worlds.contains(args[1])) {
							worlds.add(args[1]);
							saveWorlds();
							sender.sendMessage(ChatColor.GREEN + "MoreHearts has been enabled in the world '" + ChatColor.AQUA + args[1] + ChatColor.GREEN + "'");
						}
						else {
							sender.sendMessage(ChatColor.GREEN + "MoreHearts is already disabled in the world '" + ChatColor.AQUA + "'");
						}
					}
					else if (args[0].equalsIgnoreCase("addallworlds")) {
						sender.sendMessage(ChatColor.RED + "Usage: /mh addallworlds");
					} 
					else if (args[0].equalsIgnoreCase("removeworld")) {
						if (worlds.contains(args[1])) {
							worlds.remove(args[1]);
							saveWorlds();
							sender.sendMessage(ChatColor.GREEN + "MoreHearts has been disabled in the world '" + ChatColor.AQUA + args[1] + ChatColor.GREEN + "'");
						}
						else {
							sender.sendMessage(ChatColor.GREEN + "MoreHearts is already disabled in the world '" + ChatColor.AQUA + "'");
						}
					}
					else if (args[0].equalsIgnoreCase("worlds")) {
						sender.sendMessage(ChatColor.RED + "Usage: /mh worlds");
					} 
					else if (args[0].equalsIgnoreCase("reset")) {
						if (args[1].equals(getConfig().getString("ResetPassword"))) {
							getConfig().set("Players", null);
							getConfig().set("Permissions", null);
							getConfig().set("DefaultHearts", Integer.valueOf(10));
							getConfig().set("EnableIn", Bukkit.getWorlds().get(0));
							getConfig().set("HideHearts", Boolean.valueOf(true));
							saveConfig();
							refreshPerms();
							refreshWorlds();
							refreshAllPlayers();
							sender.sendMessage(ChatColor.GREEN + "Config has been reseted!");
						}
						else {
							sender.sendMessage(ChatColor.RED + "Incorrect Password");
						}
					}
					else {
						sender.sendMessage(ChatColor.RED + "Unknown Command");
					}
				}
				else if (args.length == 3) {
					if (!sender.hasPermission("morehearts." + args[0].toLowerCase())) {
						sender.sendMessage(noPerm);
					}
					else if (args[0].equalsIgnoreCase("refresh")) {
						sender.sendMessage(ChatColor.RED + "Usage: /mh refresh");
					} 
					else if (args[0].equalsIgnoreCase("add")) {
						OfflinePlayer opl = Bukkit.getOfflinePlayer(args[1]);
						if (!opl.isOnline()) {
							sender.sendMessage(ChatColor.RED + "The player '" + args[1] + "' isn't online!");
						}
						else {
							Player player = (Player)opl;
							double d = Double.parseDouble(args[2]);
							double eh = getConfig().getDouble("Players." + player.getUniqueId() + ".ExtraHearts");
							getConfig().set("Players." + player.getUniqueId() + ".ExtraHearts", Double.valueOf(eh + d));
							saveConfig();
							refreshPlayer(player);
							sender.sendMessage("" + ChatColor.GREEN + d + " hearts has been added to " + player.getName());
						}
					}
					else if (args[0].equalsIgnoreCase("set")) {
						OfflinePlayer opl = Bukkit.getOfflinePlayer(args[1]);
						if (!opl.isOnline()) {
							sender.sendMessage(ChatColor.RED + "The player '" + args[1] + "' isn't online!");
						}
						else {
							Player player = (Player)opl;
							double d = Double.parseDouble(args[2]);
							getConfig().set("Players." + player.getUniqueId() + ".ExtraHearts", Double.valueOf(d));
							saveConfig();
							refreshPlayer(player);
							sender.sendMessage(ChatColor.GREEN + player.getName() + "Extra hearts set to " + d);
						}
					}
					else if (args[0].equalsIgnoreCase("addworld")) {
						sender.sendMessage(ChatColor.RED + "Usage: /mh addworld [world]");
					}
					else if (args[0].equalsIgnoreCase("addallworlds")) {
						sender.sendMessage(ChatColor.RED + "Usage: /mh addallworlds");
					}
					else if (args[0].equalsIgnoreCase("removeworld")) {
						sender.sendMessage(ChatColor.RED + "Usage: /mh removeworld [world]");
					}
					else if (args[0].equalsIgnoreCase("worlds")) {
						sender.sendMessage(ChatColor.RED + "Usage: /mh worlds");
					}
					else if (args[0].equalsIgnoreCase("reset")) {
						sender.sendMessage(ChatColor.RED + "Usage: /mh reset <Password>");
					}
					else {
						sender.sendMessage(ChatColor.RED + "Unknown Command");
					}
				}
				else if (!sender.hasPermission("morehearts." + args[0].toLowerCase())) {
					sender.sendMessage(noPerm);
				}
				else if (args[0].equalsIgnoreCase("refresh")) {
					sender.sendMessage(ChatColor.RED + "Usage: /mh refresh");
				}
				else if (args[0].equalsIgnoreCase("add")) {
					sender.sendMessage(ChatColor.RED + "Usage: /mh add <Player> <Amount>");
				}
				else if (args[0].equalsIgnoreCase("set")) {
					sender.sendMessage(ChatColor.RED + "Usage: /mh set <Player> <Amount>");
				}
				else if (args[0].equalsIgnoreCase("addworld")) {
					sender.sendMessage(ChatColor.RED + "Usage: /mh addworld [world]");
				}
				else if (args[0].equalsIgnoreCase("addallworlds")) {
					sender.sendMessage(ChatColor.RED + "Usage: /mh addallworlds");
				}
				else if (args[0].equalsIgnoreCase("removeworld")) {
					sender.sendMessage(ChatColor.RED + "Usage: /mh removeworld [world]");
				}
				else if (args[0].equalsIgnoreCase("worlds")) {
					sender.sendMessage(ChatColor.RED + "Usage: /mh worlds");
				}
				else if (args[0].equalsIgnoreCase("reset")) {
					sender.sendMessage(ChatColor.RED + "Usage: /mh reset <Password>");
				}
				else {
					sender.sendMessage(ChatColor.RED + "Unknown Command");
				}
			}
		}
		return false;
	}
	  
	public void refreshPlayer(Player player) {
		double sum = defaultHearts;
	    if (worlds.contains(player.getWorld().getName())) {
	    	if (getConfig().contains("Players." + player.getUniqueId() + ".ExtraHearts")) {
	    		sum += getConfig().getDouble("Players." + player.getUniqueId() + ".ExtraHearts") * 2.0D;
	    	}
	    	else {
	    		getConfig().set("Players." + player.getUniqueId() + ".ExtraHearts", Integer.valueOf(0));
	    		saveConfig();
	    	}
	    	for (String str : perms.keySet()) {
	    		if (player.isPermissionSet(str)) {
	    			sum += ((Double)perms.get(str)).doubleValue();
	    		}
	    	}
	    	player.setMaxHealth(sum);
	    	player.setHealthScaled(false);
	    	if (!player.isDead()) {
	    		double hp = getConfig().getDouble("Players." + player.getUniqueId() + ".HP");
	    		if (hp == 0.0D) {
	    			player.setHealth(sum);
	    		} 
	    		else if (hp > sum) {
	    			player.setHealth(sum);
	    		} 
	    		else {
	    			player.setHealth(hp);
	    		}
	    	}
	    }
	    else {
	    	player.setMaxHealth(20.0D);
	    }
	}
	  
	public void refreshPerms() {
		perms.clear();
	    for (String str : getConfig().getConfigurationSection("Permissions").getKeys(false)) {
	    	perms.put("morehearts." + str, Double.valueOf(getConfig().getDouble("Permissions." + str) * 2.0D));
	    }
	}
	  
	public void saveWorlds() {
		String ws = (String)worlds.get(0);
	    for (int a = 1; a < worlds.size(); a++) {
	      ws = ws + "," + (String)worlds.get(a);
	    }
	    getConfig().set("EnableIn", ws);
	    saveConfig();
	}
	  
	public void refreshWorlds() {
		String ws = getConfig().getString("EnableIn");
	    String[] Eworlds = ws.split(",");
	    String[] arrayOfString1;
	    int j = (arrayOfString1 = Eworlds).length;
	    for (int i = 0; i < j; i++) {
	      String str = arrayOfString1[i];
	      
	      worlds.add(str);
	    }
	}
	  
	public void refreshAllPlayers() {
		Player[] arrayOfPlayers = Bukkit._INVALID_getOnlinePlayers();
		int numOfPlayers = arrayOfPlayers.length;
		for (int i = 0; i < numOfPlayers; i++) {
			Player players = arrayOfPlayers[i];
			refreshPlayer(players);
		}
	}
	  
	public void sendMessages(CommandSender p, String s) {
	    String[] msgs = s.split(";");
	    String[] arrayOfString1;
	    int j = (arrayOfString1 = msgs).length;
	    for (int i = 0; i < j; i++) {
	      String str = arrayOfString1[i];
	      
	      p.sendMessage(str);
	    }
	}
	  
	public Player getPlayerByUsername(String username) {
		Player player = getServer().getPlayer(username);
		return player; // Can return null if the username is invalid
	}
}