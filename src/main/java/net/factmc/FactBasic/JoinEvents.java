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
import net.alpenblock.bungeeperms.User;
import net.alpenblock.bungeeperms.platform.bukkit.event.BungeePermsUserChangedEvent;

public class JoinEvents implements Listener {
	
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
    	Player player = event.getPlayer();
    	
    	updateTeam(player, Main.getScoreboard());
    	
    	for (Player p : Bukkit.getOnlinePlayers()) {
			p.setScoreboard(Main.getScoreboard());
		}
		
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
	public void rankChange(BungeePermsUserChangedEvent event) {
    	
		Player player = Bukkit.getPlayer(event.getUser().getUUID());
		if (player != null)
			updateTeam(player, Main.getScoreboard());
		
	}
    
    
    public static void updateTeam(Player player, Scoreboard sb) {
    	if (sb == null) sb = Bukkit.getScoreboardManager().getMainScoreboard();
		
		/*if (Main.useFactions) {
			TeamManager.changeTeam(player);
		}*/
		
    	User user = BungeePerms.getInstance().getPermissionsManager().getUser(player.getUniqueId());
		Group group = user.getGroupByLadder("default");
		if (group == null) return;
		String rank = group.getName();
		if (rank.equalsIgnoreCase("default")) rank = getOtherGroup(user);
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
    
    
    public static String getOtherGroup(User user) {
    	
    	for (Group group : user.getGroups()) {
    		if (group.getLadder().equals("none"))
    			return group.getName();
    	}
    	return "default";
    	
    }
	
}