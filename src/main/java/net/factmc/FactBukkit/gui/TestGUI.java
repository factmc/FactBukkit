package net.factmc.FactBukkit.gui;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import net.factmc.FactBukkit.Main;
import net.factmc.FactCore.bukkit.InventoryControl;
import net.factmc.FactCore.bukkit.InventoryGUI;

public class TestGUI extends InventoryGUI {
	
	public static TestGUI gui = new TestGUI();
	
	protected TestGUI() {
		super(ChatColor.GREEN + "Test GUI Menu", 3, Main.getPlugin());
	}
	
	@Override
	protected void load(Player player, Inventory gui) {
		gui.setItem(13, InventoryControl.getHead(player, "This is your head!", "Click to see a message"));
	}
	
	@Override
	protected void onClick(Player player, ItemStack item) {
		if (item.getItemMeta().getDisplayName() != null && item.getItemMeta().getDisplayName().equals("This is your head!")) {
			player.sendMessage("You clicked your head!");
			player.closeInventory();
		}
	}
	
}
