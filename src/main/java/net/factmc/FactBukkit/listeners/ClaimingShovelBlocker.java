package net.factmc.FactBukkit.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class ClaimingShovelBlocker implements Listener {
	
	@EventHandler(priority = EventPriority.LOW)
	public void onClaimingShovelDig(BlockBreakEvent event) {
		
		ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
		if (item.getType() == Material.AIR) return;
		String name = item.getItemMeta().getDisplayName();
		//System.out.println(name);//DEBUG
		if (name == null) return;
		
		if (item.getType() == Material.GOLDEN_SHOVEL && name.equals(ChatColor.YELLOW + "Claiming Shovel")) {
			
			event.getPlayer().sendMessage(ChatColor.RED + "You cannot break blocks with a claiming shovel!");
			event.setCancelled(true);
			
		}
		
	}
	
}