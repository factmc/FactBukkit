package net.factmc.FactBukkit.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.factmc.FactBukkit.Main;

public class ClearLagTask extends BukkitRunnable {
	
	private static ClearLagTask instance;
	
	private int interval;
	private Integer[] warnings;
	private boolean hideMessages;
	private Class<?>[] removeTypes;
	private int counter;
	private boolean cancelled;
	
	public ClearLagTask(Plugin plugin, int interval, Integer[] warnings, boolean hideMessages, Class<?>[] removeTypes) {
		this.interval = interval;
		this.warnings = warnings;
		this.hideMessages = hideMessages;
		this.removeTypes = removeTypes;
		this.counter = 0;
		this.cancelled = false;
		
		this.runTaskTimerAsynchronously(plugin, 0, 20);
		ClearLagTask.instance = this;
	}
	
	@Override
	public void run() {
		
		if (counter == interval) {
			if (!cancelled) {
				int removed = execute();
				sendMessage(ChatColor.YELLOW + "Removed " + ChatColor.GOLD + removed + ChatColor.YELLOW + " entities");
			}
			counter = 0;
			cancelled = false;
			return;
		}
		
		if (!cancelled) {
			for (int warning : warnings) {
				if (counter == warning) {
					int remainingSec = interval - counter;
					sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Warning!"
							+ ChatColor.RED + "Ground items will be removed in " + ChatColor.GOLD + remainingSec + ChatColor.RED + " seconds"
							+ ". Use " + ChatColor.GOLD + "/clearlag cancel" + ChatColor.RED + " to cancel the next clear");
					break;
				}
			}
		}
		
		counter++;
	}
	
	private void sendMessage(String message) {
		if (hideMessages)
			Bukkit.getConsoleSender().sendMessage(message);
		else
			Bukkit.broadcastMessage(message);
	}
	
	public int execute() {
		
		List<Entity> list = new ArrayList<Entity>();
		for (World world : Bukkit.getWorlds()) {
			for (Entity entity : world.getEntitiesByClasses(removeTypes)) {
				list.add(entity);
			}
		}
		
		for (Entity entity : list) {
			entity.remove();
		}
		
		return list.size();
		
	}
	
	public boolean cancelNext() {
		if (cancelled) return false;
		cancelled = true;
		return true;
	}
	
	
	public static Class<?>[] getTypeClasses() {
		List<Class<? extends Entity>> list = new ArrayList<Class<? extends Entity>>();
		for (String type : Main.getPlugin().getConfig().getStringList("clearlag.remove-types")) {
			
			try {
				
				list.add(EntityType.valueOf(type).getEntityClass());
				
			} catch (IllegalArgumentException e) {
				Main.getPlugin().getLogger().warning(e.getMessage());
			}
			
		}
		return list.toArray(new Class<?>[list.size()]);
	}
	
	public static ClearLagTask getInstance() {
		return instance;
	}
	
}