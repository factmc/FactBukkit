package net.factmc.FactBukkit.commands;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
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
import net.factmc.FactCore.CoreUtils;
import net.factmc.FactCore.FactSQL;
import net.factmc.FactCore.bukkit.BukkitMain;

public class PointsCommand implements CommandExecutor, TabExecutor {
	
	public static final double ECON_BALANCE_PER_POINT = 1000;
	public static final DecimalFormat BALANCE_FORMAT = new DecimalFormat("#,###.##");
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("points")) {
		
			if (!(sender instanceof Player)) {
				return false;
			}
			
			if (args.length > 0) {
				
				Player player = (Player) sender;
				// Show Points
				if (args[0].equalsIgnoreCase("show")) {
					
					if (args.length > 1 && sender.hasPermission("factbukkit.stats.others")) {
		        		FactSQL.getInstance().select(FactSQL.getStatsTable(), new String[] {"UUID", "NAME", "POINTS"}, "`NAME`=?", args[1]).thenAccept((list) -> {
		        			
		        			if (list.isEmpty()) {
			        			sender.sendMessage(ChatColor.RED + args[1] + " has never joined the server");
			        		}
			        		
		        			else {
		        				
			        			UUID uuid = UUID.fromString((String) list.get(0).get("UUID"));
			        			int points = (int) list.get(0).get("POINTS");
			        			if (!uuid.equals(player.getUniqueId())) {
			        				String name = (String) list.get(0).get("NAME");
				        			sender.sendMessage(ChatColor.GREEN + name + " has " + points + " points");
				        		}
			        			
			        			else {
			        				sender.sendMessage(ChatColor.GREEN + "You have " + points + " points");
			        			}
			        			
		        			}
		        			
		        		});
		        		return true;
		        	}
					
					else {
						FactSQL.getInstance().getPoints(player.getUniqueId()).thenAccept((points) -> {
							sender.sendMessage(ChatColor.GREEN + "You have " + points + " points");
						});
						return true;
					}
					
				}
				
				// Pay Points
				else if (args[0].equalsIgnoreCase("pay")) {
					
					if (args.length < 3) {
						sender.sendMessage(ChatColor.RED + "Usage: /" + label + " pay <player> <amount>");
						return false;
					}
					
					FactSQL.getInstance().select(FactSQL.getStatsTable(), new String[] {"UUID", "NAME", "POINTS"}, "`NAME`=?", args[1]).thenAccept((list) -> {
						
						if (list.isEmpty()) {
							sender.sendMessage(ChatColor.RED + args[1] + " has never joined the server");
							return;
						}
						
						UUID to = UUID.fromString((String) list.get(0).get("UUID"));
						if (to.equals(player.getUniqueId())) {
							sender.sendMessage(ChatColor.RED + "You can not pay yourself");
							return;
						}
						
						try {
							
							int amount = Integer.parseInt(args[2]);
							if (amount < 1) throw new NumberFormatException();
							
							String toName = (String) list.get(0).get("NAME");
							FactSQL.getInstance().getPoints(player.getUniqueId()).thenAccept(points -> {
								
								if (points < amount) {
									sender.sendMessage(ChatColor.RED + "You do not have that many points");
									return;
								}
								
								FactSQL.getInstance().setPoints(player.getUniqueId(), points - amount);
								sender.sendMessage(ChatColor.GREEN + "You gave " + toName + " " + amount + " points");
								
								FactSQL.getInstance().setPoints(to, (int) (list.get(0).get("POINTS")) + amount);
								sendMessage(player, toName, ChatColor.GREEN + player.getName() + " gave you " + amount + " point" + (amount > 1 ? "s" : ""));
								
							});
							
						} catch (NumberFormatException e) {
							sender.sendMessage(ChatColor.RED + "That is not a valid number");
							return;
						}
						
					});
					return true;
					
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
						
						FactSQL.getInstance().changePoints(player.getUniqueId(), amount);
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
		
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (command.getName().equalsIgnoreCase("points")) {
			
			if (args.length < 2) return CoreUtils.filter(CoreUtils.toList("show", "pay", "convert"), args[0]);
			
			else if (args[0].equalsIgnoreCase("pay") || args[0].equalsIgnoreCase("show")) {
				
				if (args.length == 2) return CoreUtils.filter(BukkitMain.toList(Bukkit.getOnlinePlayers()), args[1]);
				
			}
			
			return CoreUtils.toList();
			
		}
		
		return null;
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