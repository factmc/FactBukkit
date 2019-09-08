package net.factmc.FactBukkit.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import me.minebuilders.clearlag.events.EntityRemoveEvent;
import net.factmc.FactBukkit.Main;

public class ClearLagCancelCommand implements Listener, CommandExecutor {
	
	private static boolean cancelNext = false;
	private static List<Entity> deathDrops = new ArrayList<Entity>();
	
	/*public ClearLagCancelCommand() {
		name = "cancel";
		argLength = 1;
		usage = "/lagg cancel";
		desc = "Cancel an automatic removal";
	}*/

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("cancelclear") && sender.hasPermission("lagg.cancel")) {
			if (sender.hasPermission("lagg.uncancel") && cancelNext) {
				cancelNext = false;
				sender.sendMessage(ChatColor.AQUA + "The next automatic clear will no longer be cancelled");
				return true;
			}
			
			cancelNext = true;
			sender.sendMessage(ChatColor.AQUA + "The next automatic clear will be cancelled");
			return true;
		}
		return false;
	}
	
	
	
	//@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		
		//Player loki = Bukkit.getPlayerExact("TrollyLoki");//DEBUG
		Location loc = event.getEntity().getLocation();
		Bukkit.getScheduler().runTaskLater(Main.getPlugin(), new Runnable() {
			@Override
			public void run() {
				
				for (Entity drop : loc.getWorld().getEntitiesByClass(Item.class)) {
					//loki.sendMessage(drop.getType() + " (" + drop.getName() + ")");//DEBUG
					if (drop.getLocation().distance(loc) <= 10) deathDrops.add(drop);
				}
				
			}
		}, 1L);
		Bukkit.getScheduler().runTaskLater(Main.getPlugin(), new Runnable() {
			@Override
			public void run() {
				
				for (Entity drop : loc.getWorld().getEntitiesByClass(ExperienceOrb.class)) {
					//loki.sendMessage(drop.getType() + " (" + drop.getName() + ")");//DEBUG
					if (drop.getLocation().distance(loc) <= 10) deathDrops.add(drop);
				}
				
			}
		}, 30L);
		
	}
	
	@EventHandler
	public void onClear(EntityRemoveEvent event) {
		
		if (cancelNext) {
			//String msg = ChatColor.RED + "Cancelling Removal...";
			//Bukkit.broadcastMessage(msg); Bukkit.getConsoleSender().sendMessage(msg);
			event.getEntityList().clear();
		}
		
		/*else {
			event.getEntityList().removeAll(deathDrops);
			deathDrops.clear();
		}*/
		
		cancelNext = false;
		
	}
	
}