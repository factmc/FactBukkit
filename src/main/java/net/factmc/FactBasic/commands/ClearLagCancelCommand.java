package net.factmc.FactBasic.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import me.minebuilders.clearlag.events.EntityRemoveEvent;
import me.minebuilders.clearlag.modules.CommandModule;

public class ClearLagCancelCommand extends CommandModule implements Listener {
	
	private static boolean cancelNext = false;
	
	public ClearLagCancelCommand() {
		name = "cancel";
		argLength = 0;
		usage = "Cancel an automatic removal";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		
		sender.sendMessage("The next automatic clear will be cancelled");
		cancelNext = true;
		return;
		
	}
	
	@EventHandler
	public void onClear(EntityRemoveEvent event) {
		
		if (cancelNext) {
			for (Entity entity : event.getEntityList()) {
				event.removeEntity(entity);
			}
		}
		
	}
	
}