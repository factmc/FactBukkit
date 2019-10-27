package net.factmc.FactBukkit.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import net.factmc.FactBukkit.JoinEvents;
import net.factmc.FactBukkit.Main;
import net.factmc.FactCore.CoreUtils;

public class ReloadCommand implements CommandExecutor, TabCompleter {
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("rtag-update")) {
        	
        	if (Main.disableRankTag) {
        		sender.sendMessage(ChatColor.RED + "Error: Rank tags are disabled");
        		return false;
        	}
        	
        	for (Player p : Bukkit.getOnlinePlayers()) {
        		
            	JoinEvents.updateTeam(p, Main.getScoreboard());
    			p.setScoreboard(Main.getScoreboard());
    			
    		}
        	
        	sender.sendMessage(ChatColor.GREEN + "Rank teams have been updated");
        	return true;
        	
        }
		return false;   
    }
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (command.getName().equalsIgnoreCase("rtag-update")) {
			
			return CoreUtils.toList();
			
		}
		
		return null;
	}

}