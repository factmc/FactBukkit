package net.factmc.FactBukkit.commands;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import net.factmc.FactBukkit.gui.StatsGUI;
import net.factmc.FactCore.CoreUtils;
import net.factmc.FactCore.bukkit.BukkitMain;
import net.md_5.bungee.api.ChatColor;

public class StatsCommand implements CommandExecutor, TabCompleter, Listener {
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("stats")) {
        	
        	if (!(sender instanceof Player)) {
        		sender.sendMessage(ChatColor.RED + "Only in-game players can do that");
        		return false;
        	}
        	Player player = (Player) sender;
        	
        	if (args.length > 0 && sender.hasPermission("facthub.stats.others")) {
        		StatsGUI.open(player, args[0]);
        		return true;
        	}
        	
        	StatsGUI.open(player, player.getName());
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