package net.factmc.FactBukkit.commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.factmc.FactBukkit.Main;
import net.factmc.FactCore.CoreUtils;
import world.bentobox.bentobox.api.flags.FlagListener;

public class SignEditCommand implements CommandExecutor, TabCompleter {
	
	private static boolean griefPrevention = false, plotSquared = false, worldGuard = false, bSkyblock = false, hub = false;
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("edit")) {
			
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Only players can use that command");
				return false;
			}
			
			if (!sender.hasPermission("factbukkit.signedit")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to do that");
				return false;
			}
			
			if (args.length < 1) {
				sender.sendMessage(ChatColor.RED + "Usage: /edit <line> [text]");
				return false;
			}
			
			int line = -1;
			try {
				line += Integer.parseInt(args[0]);
				if (line < 0|| line > 3) throw new NumberFormatException("Invalid line range");
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + "That is not a valid line");
				return false;
			}
			
			Player player = (Player) sender;
			Block block = player.getTargetBlockExact(5);
			if (block == null || !(block.getState() instanceof Sign)) {
				player.sendMessage(ChatColor.RED + "You are not looking at a sign");
				return false;
			}
			
			boolean buildAllowed = true;
			if (hub && !player.hasPermission("facthub.use")) buildAllowed = false;
			if (buildAllowed && worldGuard) {
				LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
				RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
				
				if (!WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld()) &&
						!query.testState(BukkitAdapter.adapt(block.getLocation()), localPlayer, com.sk89q.worldguard.protection.flags.Flags.BUILD)) {
					buildAllowed = false;
				}
			}
			if (buildAllowed && griefPrevention) {
				DataStore gpData = GriefPrevention.instance.dataStore;
				Claim claim = gpData.getClaimAt(block.getLocation(), false, null);
				if (claim != null && claim.allowBuild(player, block.getType()) != null)
					buildAllowed = false;
			}
			if (buildAllowed && plotSquared) {
				Location location = new Location(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
				if (location.isPlotArea()) {
					
					if (location.isPlotRoad() && !player.hasPermission("plots.admin.build.road"))
						buildAllowed = false;
					else if (location.getPlot() != null) {
						if (location.getPlot().getOwners().isEmpty() && !player.hasPermission("plots.admin.build.unowned"))
							buildAllowed = false;
						else if (!(location.getPlot().getMembers().contains(player.getUniqueId())
								|| location.getPlot().getOwners().contains(player.getUniqueId())
								|| player.hasPermission("plots.admin.build.other")))
							buildAllowed = false;
					}
				}
			}
			if (buildAllowed && bSkyblock) {
				if (!new FlagListener(){}.checkIsland(new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, new ItemStack(Material.AIR), block, BlockFace.UP),
						player, block.getLocation(), world.bentobox.bentobox.lists.Flags.PLACE_BLOCKS, true)) {
					buildAllowed = false;
				}
			}
			
			if (!buildAllowed) {
				player.sendMessage(ChatColor.RED + "You are not allowed to do that here");
				return false;
			}
			
			String text = args.length < 2 ? "" : translateColors(sender, CoreUtils.combine(args, 1));
			
			Sign sign = (Sign) block.getState();
			sign.setLine(line, text);
			sign.update();
			//player.sendMessage("Set line " + line + " on " + block.getType() + " to " + text);//DEBUG
			return true;
			
		}
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (command.getName().equalsIgnoreCase("edit")) {
			
			if (args.length < 2) return CoreUtils.filter(CoreUtils.toList("1", "2", "3", "4"), args[0]);
			
			return CoreUtils.toList();
			
		}
		
		return null;
	}
	
	
	public static String translateColors(CommandSender sender, String text) {
		
		List<ChatColor> toTranslate = new ArrayList<ChatColor>();
		
		for (ChatColor color : ChatColor.values()) {
			if (sender.hasPermission("essentials.signs.color") && color.isColor())
				toTranslate.add(color);
			if (sender.hasPermission("essentials.signs.format") && (color.isFormat() || color.equals(ChatColor.RESET))
					&& !color.equals(ChatColor.MAGIC))
				toTranslate.add(color);
			if (sender.hasPermission("essentials.signs.magic") && color.equals(ChatColor.MAGIC))
				toTranslate.add(color);
		}
		
		for (ChatColor color : toTranslate) {
			text = text.replaceAll("&" + color.getChar(), "" + color);
		}
		return text;
		
	}
	
	
	public static void load() {
		
		if (Bukkit.getPluginManager().getPlugin("GriefPrevention") != null) griefPrevention = true;
		if (Bukkit.getPluginManager().getPlugin("PlotSquared") != null) plotSquared = true;
		if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) worldGuard = true;
		if (Bukkit.getPluginManager().getPlugin("FactHub") != null) hub = true;
		
		Main.getPlugin().getCommand("edit").setExecutor(new SignEditCommand());
		
	}
	
}