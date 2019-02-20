package net.factmc.FactBasic.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import net.factmc.FactBasic.JoinEvents;
import net.factmc.FactBasic.Main;

public class ReloadCommand implements CommandExecutor {
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("rtag-update")) {
        	
        	if (Main.disableRankTag) {
        		sender.sendMessage(ChatColor.RED + "Error: Rank tags are disabled");
        		return false;
        	}
        	
        	Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        	
        	for (Player p : Bukkit.getOnlinePlayers()) {
        		
            	JoinEvents.updateTeam(p, null);
            	
    			p.setScoreboard(sb);
    			
    		}
        	
        	sender.sendMessage(ChatColor.GREEN + "Rank teams have been updated");
        	return true;
        	
        }
		return false;   
    }

}