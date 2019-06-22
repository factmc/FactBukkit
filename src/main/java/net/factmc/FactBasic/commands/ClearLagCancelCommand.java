package net.factmc.FactBasic.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import me.minebuilders.clearlag.events.EntityRemoveEvent;

public class ClearLagCancelCommand implements Listener, CommandExecutor {
	
	private static boolean cancelNext = false;
	
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
	
	@EventHandler
	public void onClear(EntityRemoveEvent event) {
		
		if (cancelNext) {
			//String msg = ChatColor.RED + "Cancelling Removal...";
			//Bukkit.broadcastMessage(msg); Bukkit.getConsoleSender().sendMessage(msg);
			event.getEntityList().clear();
		}
		cancelNext = false;
		
	}
	
}