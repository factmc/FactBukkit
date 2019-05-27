package net.factmc.FactBasic.supervanish;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import de.myzelyam.api.vanish.VanishAPI;
import net.factmc.FactBasic.JoinEvents;
import net.factmc.FactBasic.Main;

public class VanishEvents implements Listener {
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void vanishChange(PlayerHideEvent event) {
		if (!Main.disableRankTag) {
			Bukkit.getScheduler().runTaskLater(Main.getPlugin(), new Runnable() {
				@Override
				public void run() {
					JoinEvents.updateTeam(event.getPlayer(), Main.getScoreboard());
				}
			}, 5L);
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void vanishChange(PlayerShowEvent event) {
		if (!Main.disableRankTag) {
			Bukkit.getScheduler().runTaskLater(Main.getPlugin(), new Runnable() {
				@Override
				public void run() {
					JoinEvents.updateTeam(event.getPlayer(), Main.getScoreboard());
				}
			}, 5L);
		}
	}
	
	
	public static List<SendPause> msgs = new ArrayList<SendPause>();
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChatMessage(AsyncPlayerChatEvent event) {
		
		if (event.isCancelled()) return;
		
		Player player = event.getPlayer();
		
		SendPause pause = null;
		if (VanishAPI.isInvisible(player)) {
			
			
			for (SendPause msg : msgs) {
				
				if (matches(player, msg, event.getMessage())) {
					
					pause = msg;
					break;
					
				}
			}
		
			if (pause != null) {
				event.setCancelled(false);
				msgs.remove(pause);
			}
			
			else {
				event.setCancelled(true);
				
				final SendPause newPause = new SendPause(player, event.getMessage());
				Bukkit.getScheduler().runTask(Main.getPlugin(), new Runnable() {

					@Override
					public void run() {
						msgs.add(newPause);
					}
					
				});
				
				player.sendMessage(ChatColor.GREEN + "You are vanished! To confirm sending \""
						+ newPause.getMessage() + ChatColor.GREEN + "\" type it again");
				
			}
			
			
			SendPause old = null;
			for (SendPause msg : msgs) {
				if (msg.getPlayer() == player) {
					old = msg;
					break;
				}
			}
			
			if (old != null) {
				msgs.remove(old);
			}
			
			
		}
		
	}
	
	
	public static boolean matches(Player player, SendPause pause, String msg) {
		
		if (player != pause.getPlayer()) return false;
		
		if (pause.getMessage().equals(msg)) {
			return true;
		}
		
		else {
			return false;
		}
		
	}
 	
}