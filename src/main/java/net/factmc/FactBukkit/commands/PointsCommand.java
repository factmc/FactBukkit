package net.factmc.FactBukkit.commands;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import net.factmc.FactBukkit.Main;
import net.factmc.FactCore.FactSQLConnector;

public class PointsCommand implements CommandExecutor, TabExecutor {
	
	public static final double ECON_BALANCE_PER_POINT = 1000;
	public static final DecimalFormat BALANCE_FORMAT = new DecimalFormat("#,###.##");
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if (!(sender instanceof Player)) {
			return false;
		}
		
		if (args.length > 0) {
			
			Player player = (Player) sender;
			// Show Points
			if (args[0].equalsIgnoreCase("show")) {
				
				if (args.length > 1 && sender.hasPermission("factbukkit.stats.others")) {
	        		UUID uuid = FactSQLConnector.getUUID(args[1]);
	        		if (uuid == null) {
	        			sender.sendMessage(ChatColor.RED + args[1] + " has never joined the server");
						return false;
	        		}
	        		
	        		if (!uuid.equals(player.getUniqueId())) {
		        		int points = FactSQLConnector.getPoints(uuid);
						sender.sendMessage(ChatColor.GREEN + FactSQLConnector.getName(uuid) + " has " + points + " points");
						return true;
	        		}
	        	}
				
				int points = FactSQLConnector.getPoints(player.getUniqueId());
				sender.sendMessage(ChatColor.GREEN + "You have " + points + " points");
				//TestGUI.gui.open(player);//DEBUG
				return true;
				
			}
			
			// Pay Points
			else if (args[0].equalsIgnoreCase("pay")) {
				
				if (args.length < 3) {
					sender.sendMessage(ChatColor.RED + "Usage: /" + label + " pay <player> <amount>");
					return false;
				}
				
				UUID to = FactSQLConnector.getUUID(args[1]);
				if (to == null) {
					sender.sendMessage(ChatColor.RED + args[1] + " has never joined the server");
					return false;
				}
				else if (to.equals(player.getUniqueId())) {
					sender.sendMessage(ChatColor.RED + "You can not pay yourself");
					return false;
				}
				
				try {
					
					int amount = Integer.parseInt(args[2]);
					if (amount < 1) throw new NumberFormatException();
					if (FactSQLConnector.getPoints(player.getUniqueId()) < amount) {
						player.sendMessage(ChatColor.RED + "You do not have that many points");
						return false;
					}
					
					FactSQLConnector.changePoints(player.getUniqueId(), -amount);
					String toName = FactSQLConnector.getName(to);
					sender.sendMessage(ChatColor.GREEN + "You gave " + toName + " " + amount + " points");
					
					FactSQLConnector.changePoints(to, amount);
					sendMessage(player, toName, ChatColor.GREEN + player.getName() + " gave you " + amount + " point" + (amount > 1 ? "s" : ""));
					return true;
					
				} catch (NumberFormatException e) {
					sender.sendMessage(ChatColor.RED + "That is not a valid number");
					return false;
				}
				
			}
			
			// Convert Economy Balance to Points
			else if (args[0].equalsIgnoreCase("convert")) {
				
				if (Main.econ == null) {
					sender.sendMessage(ChatColor.RED + "This server does not have an economy");
					return false;
				}
				
				if (args.length < 2) {
					sender.sendMessage(ChatColor.RED + "Usage: /" + label + " convert <amount>");
					return false;
				}
				
				try {
					
					int amount = Integer.parseInt(args[1]);
					if (amount < 0) throw new NumberFormatException();
					double requiredBalance = ((double) amount) * ECON_BALANCE_PER_POINT;
					if (!Main.econ.has(player, requiredBalance)) {
						player.sendMessage(ChatColor.RED + "You do not have $" + BALANCE_FORMAT.format(requiredBalance));
						return false;
					}
					
					Main.econ.withdrawPlayer(player, requiredBalance);
					
					FactSQLConnector.changePoints(player.getUniqueId(), amount);
					sender.sendMessage(ChatColor.GREEN + "You have converted $" + BALANCE_FORMAT.format(requiredBalance) + " into " + amount + " points");
					return true;
					
				} catch (NumberFormatException e) {
					sender.sendMessage(ChatColor.RED + "That is not a valid number");
					return false;
				}
				
			}
			
		}
		
		// Points Command Help
		sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <show|pay|convert>");
		return false;
		
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		
		if (args.length > 0) {
			
			if (args[0].equalsIgnoreCase("pay") || (args[0].equalsIgnoreCase("show") && sender.hasPermission("factbukkit.stats.others"))) {
				
				if (args.length == 2) {
					return filter(toList(Bukkit.getOnlinePlayers()), args[1]);
				}
				return toList();
				
			}
			
			else if (args[0].equalsIgnoreCase("convert") || args[0].equalsIgnoreCase("show")) {
				return toList();
			}
			
			return filter(toList("show", "pay", "convert"), args[0]);
			
		}
		
		return toList("show", "pay", "convert");
		
	}
	
	
	
	public static List<String> toList(String... strings) {
		
		List<String> list = new ArrayList<String>();
		for (String string : strings) {
			list.add(string);
		}
		return list;
		
	}
	public static List<String> toList(Collection<? extends Player> collection) {
		
		List<String> list = new ArrayList<String>();
		for (Player player : collection) {
			list.add(player.getName());
		}
		return list;
		
	}
	public static List<String> filter(List<String> list, String start) {
		if (start.equals("")) return list;
		List<String> filtered = new ArrayList<String>();
		for (String string : list) {
			if (string.toLowerCase().startsWith(start.toLowerCase())) {
				filtered.add(string);
			}
		}
		return filtered;
	}
	
	
	public static void sendMessage(Player player, String to, String message) {
		
		Bukkit.getScheduler().runTask(Main.getPlugin(), new Runnable() {
			@Override
			public void run() {
				try (
						ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
						DataOutputStream dos = new DataOutputStream(baos)
					){
					
			        dos.writeUTF("Message");
			        dos.writeUTF(to);
			        dos.writeUTF(message);
			        player.sendPluginMessage(Main.getPlugin(), "BungeeCord", baos.toByteArray());
				} catch (IOException e){
					e.printStackTrace();
				}
				
			}
		});
		
	}
	
}