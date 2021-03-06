package net.factmc.FactBukkit.gui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import net.factmc.FactBukkit.Main;
import net.factmc.FactCore.CoreUtils;
import net.factmc.FactCore.FactSQL;
import net.factmc.FactCore.bukkit.InventoryControl;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;

public class StatsGUI implements Listener {
	
	private static boolean loaded = false;
	
	public static void open(Player player, String inputName) {
		FactSQL.getInstance().select(FactSQL.getStatsTable(), new String[] {"UUID", "NAME", "PLAYTIME", "POINTS", "TOTALVOTES"}, "`NAME`=?", inputName).thenAccept((list) -> {
			
			if (list.isEmpty()) {
				player.sendMessage(ChatColor.RED + "Unable to get data for " + inputName);
			}
			
			else {
				UUID uuid = UUID.fromString((String) list.get(0).get("UUID"));
				CoreUtils.getColoredRank(uuid).thenAccept((coloredRank) -> {
					
					String name = (String) list.get(0).get("NAME");
					long playtime = (long) list.get(0).get("PLAYTIME");
					int points = (int) list.get(0).get("POINTS");
					int totalVotes = (int) list.get(0).get("TOTALVOTES");
					Bukkit.getScheduler().runTask(Main.getPlugin(), () -> open(player, name, uuid, coloredRank, playtime, points, totalVotes));
					
				});
			}
			
		});
	}
	
	public static void open(Player player, String name, UUID uuid, String rank, long playtimeSeconds, int pointsInt, int totalVotes) {
		
		Inventory gui = player.getServer().createInventory(player, 45, ChatColor.BLUE + name + "'s Stats");
		ItemStack head = InventoryControl.getHead(name, name);
		
		String playtime = CoreUtils.convertSeconds(playtimeSeconds);
		ItemStack role = InventoryControl.getItemStack(Material.ENDER_EYE,
				"&aRank: " + rank,
				"&9Playtime: &b" + playtime);
		
		if (player.getName().equals(name)) {
			ItemStack upgrade = InventoryControl.getItemStack(Material.TOTEM_OF_UNDYING, "&dRank Upgrade", rankUpgradeStatus(uuid, playtimeSeconds));
					
			gui.setItem(32, upgrade);
		}
		
		ItemStack points = InventoryControl.getItemStack(Material.SUNFLOWER,
				"&6Points: &e" + pointsInt,
				"&aYou can earn more by &nvoting&a!");
		ItemStack votes = InventoryControl.getItemStack(Material.CLOCK,
				"&3Total Votes: &b" + totalVotes,
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
	
	@EventHandler(priority = EventPriority.HIGH)
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
				FactSQL.getInstance().get(FactSQL.getStatsTable(), player.getUniqueId(), "PLAYTIME").thenAccept((playtime) -> {
					
					String upgradeGroup = rankUpgradeGroup(player.getUniqueId(), (long) playtime);
					if (upgradeGroup != null) {
						User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
						Set<Node> groups = user.getNodes().stream().filter(NodeType.INHERITANCE::matches).collect(Collectors.toSet());
						groups.forEach(node -> user.data().remove(node));
						
						Node node = InheritanceNode.builder(upgradeGroup).build();
						user.data().add(node);
						LuckPermsProvider.get().getUserManager().saveUser(user);
						open(player, player.getName());
						broadcastUpgrade(player, CoreUtils.getColoredRank(upgradeGroup));
					}
					
				});
			}
			
		}
	}
	
	
	public static String rankUpgradeGroup(UUID uuid, long playtime) {
		
		String currentGroup = LuckPermsProvider.get().getUserManager().getUser(uuid).getPrimaryGroup();
		ConfigurationSection playtimeRanks = Main.getPlugin().getConfig().getConfigurationSection("playtime-ranks");
		for (String k : playtimeRanks.getKeys(false)) {
			
			if (currentGroup.equals(playtimeRanks.getString(k + ".prerequisite"))) {
				
				int requiredPlaytime = playtimeRanks.getInt(k + ".playtime");
				if (playtime >= requiredPlaytime) {
					return k;
				}
				break;
				
			}
			
		}
		
		return null;
	}
	
	public static List<String> rankUpgradeStatus(UUID uuid, long playtime) {
		
		long remaining = 0;
		String rank = "";
		List<String> list = new ArrayList<String>();
		
		String currentGroup = LuckPermsProvider.get().getUserManager().getUser(uuid).getPrimaryGroup();
		ConfigurationSection playtimeRanks = Main.getPlugin().getConfig().getConfigurationSection("playtime-ranks");
		for (String k : playtimeRanks.getKeys(false)) {
			
			if (currentGroup.equals(playtimeRanks.getString(k + ".prerequisite"))) {
				
				rank = CoreUtils.getColoredRank(k);
				int requiredPlaytime = playtimeRanks.getInt(k + ".playtime");
				if (playtime < requiredPlaytime) {
					remaining = requiredPlaytime - playtime;
				}
				break;
				
			}
			
		}
		
		if (rank.equals("")) {
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
			list.addAll(displaySeconds(remaining, rank));
			return list;
		}
		
	}
	
	public static List<String> displaySeconds(long seconds, String rank) {
		List<String> list = new ArrayList<String>();
		
		long mins = Math.round(seconds / 60.0);
		long minutes = mins % 60;
		long hours = (mins - minutes) / 60;
		
		if (hours > 0) {
			list.add(ChatColor.RED + "" + hours + " hours and " + minutes + " minutes of");
			list.add(ChatColor.RED + "playtime to claim " + rank + ChatColor.RED + " rank!");
		}
		else {
			list.add(ChatColor.RED + "" + minutes + " minutes of playtime");
			list.add(ChatColor.RED + "to claim " + rank + ChatColor.RED + " rank!");
		}
		
		return list;
		
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