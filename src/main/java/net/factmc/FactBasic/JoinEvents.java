package net.factmc.FactBasic;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import de.myzelyam.api.vanish.VanishAPI;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.platform.bukkit.event.BungeePermsUserChangedEvent;

public class JoinEvents implements Listener {
	
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
    	Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
    	Player player = event.getPlayer();
    	
    	updateTeam(player, null);
    	
    	for (Player p : Bukkit.getOnlinePlayers()) {
			p.setScoreboard(sb);
		}
		
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
	public void rankChange(BungeePermsUserChangedEvent event) {
    	
		Player player = Bukkit.getPlayer(event.getUser().getUUID());
		if (player != null) {
			updateTeam(player, null);
		}
		
	}
    
    
    public static void updateTeam(Player player, Scoreboard sb) {
    	if (sb == null) sb = Bukkit.getScoreboardManager().getMainScoreboard();
		
		/*if (Main.useFactions) {
			TeamManager.changeTeam(player);
		}*/
		
		Group group = BungeePerms.getInstance().getPermissionsManager().getUser(player.getUniqueId()).getGroupByLadder("default");
		if (group == null) return;
		String rank = group.getName();
		String add = "";
		if (VanishAPI.isInvisible(player)) add = "v";
		
		if (rank.equalsIgnoreCase("default")) {
			if (add.equals("v")) {
				Team team = sb.getTeam("rank_vanished");
				team.addEntry(player.getName());
			}
			else {
				Team team = sb.getEntryTeam(player.getName());
				if (team == null) return;
				team.removeEntry(player.getName());
			}
		}
		else {
			Team team = sb.getTeam("rank_" + rank + add);
			team.addEntry(player.getName());
		}
			
    }
	
}