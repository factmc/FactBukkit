package net.factmc.FactBukkit.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import net.factmc.FactBukkit.gui.StatsGUI;
import net.factmc.FactCore.CoreUtils;
import net.factmc.FactCore.FactSQL;
import net.factmc.FactCore.bukkit.BukkitMain;
import net.md_5.bungee.api.ChatColor;

public class StatsCommand implements CommandExecutor, TabCompleter, Listener {
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("stats")) {
        	
        	boolean isPlayer = false;
        	Player player = null;
        	if (sender instanceof Player) {
        		isPlayer = true;
        		player = (Player) sender;
        	}
        	if (!isPlayer) {
        		sender.sendMessage(ChatColor.RED + "Only in-game players can do that");
        		return false;
        	}
        	
        	if (args.length == 0) {
        		if (isPlayer) StatsGUI.open(player, player.getName());
        		return true;
        	}
        	
        	else if (args.length > 0) {
        		if (!sender.hasPermission("facthub.stats.others")) {
        			if (isPlayer) StatsGUI.open(player, player.getName());
        			return true;
        		}
        		
        		UUID uuid = FactSQL.getInstance().getUUID(args[0]);
        		if (uuid == null) {
        			sender.sendMessage(ChatColor.RED + "Unable to get data for " + args[0]);
        			return false;
        		}
        		StatsGUI.open(player, args[0]);
        		return true;
        	}
        	
        	if (isPlayer) StatsGUI.open(player, player.getName());
        	return true;
        	
        }
        
		return false;   
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (command.getName().equalsIgnoreCase("stats")) {
			
			if (args.length < 2) return CoreUtils.filter(BukkitMain.toList(Bukkit.getOnlinePlayers()), args[0]);
			
			return CoreUtils.toList();
			
		}
		
		return null;
	}

}