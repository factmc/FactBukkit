package net.factmc.FactBasic;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import de.myzelyam.api.vanish.VanishAPI;
import me.lucko.luckperms.LuckPerms;

public class JoinEvents implements Listener {
	
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
    	Player player = event.getPlayer();
    	
    	updateTeam(player, Main.getScoreboard());
    	
    	for (Player p : Bukkit.getOnlinePlayers()) {
			p.setScoreboard(Main.getScoreboard());
		}
		
    }
    
    
    public static void updateTeam(Player player, Scoreboard sb) {
    	if (sb == null) sb = Bukkit.getScoreboardManager().getMainScoreboard();
		
		/*if (Main.useFactions) {
			TeamManager.changeTeam(player);
		}*/
		
    	String rank = LuckPerms.getApi().getUser(player.getUniqueId()).getPrimaryGroup();
		String add = "";
		if (VanishAPI.isInvisible(player)) add = "v";
		
		/*if (rank.equalsIgnoreCase("default")) {
			if (add.equals("v")) {
				Team team = sb.getTeam("rank_vanished");
				team.addEntry(player.getName());
			}
			else {
				Team team = sb.getEntryTeam(player.getName());
				if (team == null) return;
				team.removeEntry(player.getName());
			}
		}*/
		Team team = sb.getTeam("rank_" + rank + add);
		team.addEntry(player.getName());
			
    }
	
}