package net.factmc.FactBukkit.gui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.Node;
import me.lucko.luckperms.api.User;
import net.factmc.FactBukkit.Main;
import net.factmc.FactCore.CoreUtils;
import net.factmc.FactCore.FactSQLConnector;
import net.factmc.FactCore.bukkit.InventoryControl;

public class StatsGUI implements Listener {
	
	public static final int VIP_PLAYTIME = 172800; // 48 Hours
	public static final int MVP_PLAYTIME = 604800; // 168 Hours (7 days)
	
	
	private static boolean loaded = false;
	
	public static void open(Player player, String name) {
		Inventory gui = player.getServer().createInventory(player, 45, ChatColor.BLUE + name + "'s Stats");
		
		UUID uuid = player.getUniqueId();
		if (!player.getName().equals(name)) {
			uuid = FactSQLConnector.getUUID(name);
		}
		
		ItemStack head = InventoryControl.getHead(name, "&9Statistics of " + name);
		
		String rank = CoreUtils.getColoredRank(uuid);
		String group = CoreUtils.getRankGroup(uuid);
		ItemStack role = InventoryControl.getItemStack(Material.ENDER_EYE,
				"&2Rank: " + rank,
				"&5Group: &d" + group);
		
		if (player.getName().equals(name)) {
			ItemStack upgrade = InventoryControl.getItemStack(Material.TOTEM_OF_UNDYING, "&dRank Upgrade", rankUpgradeStatus(uuid));
					
			gui.setItem(32, upgrade);
		}
		
		ItemStack points = InventoryControl.getItemStack(Material.DIAMOND,
				"&6Points: &e" + FactSQLConnector.getPoints(uuid),
				"&aYou can earn more by &nvoting&a!");
		ItemStack votes = InventoryControl.getItemStack(Material.CLOCK,
				"&3Total Votes: &b" + FactSQLConnector.getIntValue(FactSQLConnector.getStatsTable(), uuid, "TOTALVOTES"),
				"&7Click to vote");
		ItemStack divider = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
		
		int i = 9;
		while (i < 18) {
			gui.setItem(i, divider);
			i++;
		}
		
		gui.setItem(4, head);
		gui.setItem(28, role);
		gui.setItem(player.getName().equals(name) ? 30 : 31, points);
		gui.setItem(34, votes);
		
		loaded = true;
		player.openInventory(gui);
	}
	
	@EventHandler
	public void itemClicked(InventoryClickEvent event) {
		if (!loaded) return;
		final Player player = (Player) event.getWhoClicked();
		if (event.getView().getTitle().endsWith("'s Stats")) {
			
			event.setCancelled(true);
			if ((event.getCurrentItem() == null) || (event.getCurrentItem().getType().equals(Material.AIR))) {
                return;
            }
			//String name = event.getCurrentItem().getItemMeta().getDisplayName();
			List<String> loreList = event.getCurrentItem().getItemMeta().getLore();
			if (loreList == null) return;
			String lore = loreList.get(0);
			
			if (lore.equalsIgnoreCase(ChatColor.GRAY + "Click to vote")) {
				player.performCommand("vote");
				player.closeInventory();
			}
			
			else if (loreList.size() == 4) {
				String upgradeGroup = rankUpgradeGroup(player.getUniqueId());
				if (upgradeGroup != null) {
					try {
						User user = LuckPerms.getApi().getUserManager().loadUser(player.getUniqueId()).get();
						user.clearParents();
						Node node = LuckPerms.getApi().getNodeFactory().makeGroupNode(upgradeGroup).build();
						user.setPermission(node);
						LuckPerms.getApi().getUserManager().saveUser(user);
						open(player, player.getName());
						broadcastUpgrade(player, CoreUtils.getColoredRank(upgradeGroup));
					} catch (ExecutionException | InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
		}
	}
	
	
	public static String rankUpgradeGroup(UUID uuid) {
		long playtime = FactSQLConnector.getLongValue(FactSQLConnector.getStatsTable(), uuid, "PLAYTIME");
		
		if (playtime > VIP_PLAYTIME && LuckPerms.getApi().getUser(uuid).getPrimaryGroup().equals("default")) {
			return "vip";
		}

		else if (playtime > MVP_PLAYTIME && LuckPerms.getApi().getUser(uuid).getPrimaryGroup().equals("vip")) {
			return "mvp";
		}
		
		return null;
	}
	
	public static List<String> rankUpgradeStatus(UUID uuid) {
		
		long playtime = FactSQLConnector.getLongValue(FactSQLConnector.getStatsTable(), uuid, "PLAYTIME");
		long remaining = 0;
		String rank = "";
		List<String> list = new ArrayList<String>();
		
		if (playtime < VIP_PLAYTIME) {
			remaining = VIP_PLAYTIME - playtime;
			rank = CoreUtils.getColoredRank("vip");
		}
		else if (playtime > VIP_PLAYTIME && LuckPerms.getApi().getUser(uuid).getPrimaryGroup().equals("default")) {
			rank = CoreUtils.getColoredRank("vip");
		}
		
		else if (playtime < MVP_PLAYTIME) {
			remaining = MVP_PLAYTIME - playtime;
			rank = CoreUtils.getColoredRank("mvp");
		}
		else if (playtime > MVP_PLAYTIME && LuckPerms.getApi().getUser(uuid).getPrimaryGroup().equals("vip")) {
			rank = CoreUtils.getColoredRank("mvp");
		}
		
		else {
			list.add(ChatColor.AQUA + "You have already claimed");
			list.add(ChatColor.AQUA + "all available ranks");
			list.add(ChatColor.AQUA + "Thanks for the support!");
			return list;
		}
		
		
		if (remaining == 0) {
			list.add(ChatColor.GREEN + "Congratulations! You");
			list.add(ChatColor.GREEN + "have enough playtime to");
			list.add(ChatColor.GREEN + "claim " + rank + ChatColor.GREEN + " rank!");
			list.add(ChatColor.GRAY + "Click here to claim it");
			return list;
		}
		else {
			list.add(ChatColor.RED + "You need to have another");
			list.add(ChatColor.RED + CoreUtils.convertSeconds(remaining) + (remaining >= 3600 ? " of" : " of playtime"));
			list.add(ChatColor.RED + (remaining >= 3600 ? "playtime " : "") + "to claim " + rank + ChatColor.RED + " rank!");
			return list;
		}
		
	}
	
	
	public static void broadcastUpgrade(Player player, String rank) {
		
		Bukkit.getScheduler().runTask(Main.getPlugin(), new Runnable() {
			@Override
			public void run() {
				try (
						ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
						DataOutputStream dos = new DataOutputStream(baos)
					){
					
			        dos.writeUTF("Message");
			        dos.writeUTF("ALL");
			        dos.writeUTF(ChatColor.GREEN + "Congratulations " + player.getName() + " for advancing to " + rank + ChatColor.GREEN + " rank!");
			        player.sendPluginMessage(Main.getPlugin(), "BungeeCord", baos.toByteArray());
				} catch (IOException e){
					e.printStackTrace();
				}
				
			}
		});
		
	}
	
}