package net.factmc.FactBasic;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import net.alpenblock.bungeeperms.platform.bukkit.event.BungeePermsUserChangedEvent;

public class JoinEvents implements Listener {
	
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
    	Player player = event.getPlayer();
    	Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
    	
    	updateTeam(player, sb);
    	
    	for (Player p : Bukkit.getOnlinePlayers()) {
			p.setScoreboard(sb);
		}
		
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
	public void rankChange(BungeePermsUserChangedEvent event) {
		
		Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
		Player player = Bukkit.getPlayer(event.getUser().getUUID());
		if (player != null) {
			JoinEvents.updateTeam(player, sb);
		}
		
	}
    
    
    public static void updateTeam(Player player, Scoreboard sb) {
		
		/*if (Main.useFactions) {
			TeamManager.changeTeam(player);
		}*/
		
		String rank = Main.perms.getPrimaryGroup(player);
		if (rank == null) return;
		if (rank.equalsIgnoreCase("default")) {
			Team team = sb.getEntryTeam(player.getName());
			if (team == null) return;
			team.removeEntry(player.getName());
		}
		else {
			Team team = sb.getTeam("rank-" + rank);
			team.addEntry(player.getName());
		}
			
    }
	
}