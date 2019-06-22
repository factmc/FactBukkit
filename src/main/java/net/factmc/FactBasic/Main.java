package net.factmc.FactBasic;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Group;
import net.factmc.FactBasic.commands.ClearLagCancelCommand;
import net.factmc.FactBasic.commands.ReloadCommand;
import net.factmc.FactBasic.listeners.ClaimingShovelBlocker;
import net.factmc.FactBasic.listeners.VanishEvents;
import net.factmc.FactCore.CoreUtils;

public class Main extends JavaPlugin implements Listener {
	
	public static JavaPlugin plugin;
	
	//public static List<Object> roles = new ArrayList<Object>();
	
	public static boolean useVanish = true;
	public static boolean gpInstalled = false;
	public static boolean disableRankTag = false;
	private static Scoreboard sb;
	
    @Override
    public void onEnable() {
    	plugin = this;
    	
    	/*useFactions = getServer().getPluginManager().getPlugin("Factions") != null;
    	if (useFactions) {
    		plugin.getLogger().info("Found Factions. Using Factions for prefixes");
    		
    		roles.clear();
            roles.add(Rel.RECRUIT);
            roles.add(Rel.MEMBER);
            roles.add(Rel.OFFICER);
            roles.add(Rel.LEADER);
    	}*/
    	
    	useVanish = getServer().getPluginManager().getPlugin("SuperVanish") != null;
    	if (useVanish) {
    		plugin.getLogger().info("Found SuperVanish. Monitoring its events");
    	}
    	
    	gpInstalled = getServer().getPluginManager().getPlugin("GriefPrevention") != null;
    	if (gpInstalled) {
    		plugin.getLogger().info("Blocking claiming shovel mining");
    	}
    	
    	/*
    	disableRankTag = getServer().getPluginManager().getPlugin("FactHub") != null;
    	if (disableRankTag) {
    		plugin.getLogger().info("Found FactHub. Disabling Listeners");
    	}
    	*/
    	
    	Bukkit.getPluginCommand("rtag-update").setExecutor(new ReloadCommand());
    	registerEvents();
    	registerCommands();
    	
    	/*RegisteredServiceProvider<Permission> permRSP = getServer().getServicesManager().getRegistration(Permission.class);
        perms = permRSP.getProvider();
        plugin.getLogger().info("Connected to " + perms.getName());*/
    	
        if (!disableRankTag) {
        	sb = Bukkit.getScoreboardManager().getMainScoreboard();
	    	registerTeams(sb);
	        plugin.getLogger().info("Registered Teams");
        }
    }
    
    @Override
    public void onDisable() {
    	Bukkit.getScheduler().cancelTasks(plugin);
    	plugin.getLogger().info("Cancelled Tasks");
    	
    	plugin = null;
    }
    
    public void registerEvents() {
    	
    	if (useVanish) {
    		getServer().getPluginManager()
    				.registerEvents(new VanishEvents(), plugin);
    	}
    	
    	if (gpInstalled) {
    		getServer().getPluginManager()
    				.registerEvents(new ClaimingShovelBlocker(), plugin);
    	}
    	
    	if (!disableRankTag) {
	    	getServer().getPluginManager()
	    			.registerEvents(new JoinEvents(), plugin);
    	}
    	
    }
    
    public void registerCommands() {
    	//plugin.getCommand("cmd").setExecutor(this);
    	if (getServer().getPluginManager().getPlugin("ClearLag") != null) {
    		getServer().getPluginCommand("cancelclear").setExecutor(new ClearLagCancelCommand());
    		getServer().getPluginManager().registerEvents(new ClearLagCancelCommand(), plugin);
    		plugin.getLogger().info("Registered ClearLag cancel command");
    	}
    }
    
    public static JavaPlugin getPlugin() {
        return plugin;
    }
    
    public static Scoreboard getScoreboard() {
    	return sb;
    }
    
    
    public static void registerTeams(Scoreboard sb) {
    	
    	/*if (useFactions) {
    		for (Team team : sb.getTeams()) {
        		if (team.getName().startsWith("f")) {
        			team.unregister();
        		}
        	}
    		
    		for (Faction faction : FactionColl.get().getAll()) {
    			TeamManager.updateTeam(faction);
    		}
    	}*/
    	
		for (Group raw : BungeePerms.getInstance().getPermissionsManager().getGroups()) {
			String group = raw.getName();
    		for (int i = 0; i < 2; i++) {
    			String add = "";
    			if (i > 0) {
    				if (!useVanish) break;
    				add = "v";
    			}
    			
        		Team team; String teamName = "rank_" + group + add;
	        	try {
	        		team = sb.registerNewTeam(teamName);
	        	} catch (IllegalArgumentException e) {
	        		team = sb.getTeam(teamName);
	        	}
	        	
	        	if (!group.equalsIgnoreCase("default")) {
		        	String name = CoreUtils.getColoredRank(group);
		        	String prefix = "[" + name + ChatColor.RESET + "]";
		        	team.setPrefix(prefix);
	        	}
	        	if (i > 0) {
	        		team.setSuffix(ChatColor.GRAY + "[HIDDEN]");
	        		team.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);
	        	}
	        	
    		}
        }
		
		/*if (useVanish) {
			Team oldTeam = sb.getTeam("rank_vanished");
    		if (oldTeam != null) oldTeam.unregister();
        	Team team = sb.registerNewTeam("rank_vanished");
    		team.setSuffix(ChatColor.GRAY + "[HIDDEN]");
		}*/
    	
    }
    
    @Deprecated
    public static String getColoredRank(String rank) {
    	rank = rank.toLowerCase();
    	
    	ChatColor color;
    	String name;
    	switch (rank) {
    	
    	case "vip":
    		color = ChatColor.LIGHT_PURPLE;
    		name = "VIP";
    		break;
    	case "mvp":
    		color = ChatColor.AQUA;
    		name = "MVP";
    		break;
    	case "youtube":
    		return ChatColor.BOLD + "You"
    		+ ChatColor.RED + ChatColor.BOLD + "Tube";
    	case "helper":
    		color = ChatColor.GREEN;
    		name = "Helper";
    		break;
    	case "mod":
    		color = ChatColor.YELLOW;
    		name = "Mod";
    		break;
    	case "admin":
    		color = ChatColor.RED;
    		name = "Admin";
    		break;
    	case "head-admin":
    		color = ChatColor.DARK_RED;
    		name = "Head Admin";
    		break;
    	case "owner":
    		color = ChatColor.GOLD;
    		name = "Owner";
    		break;
    	default:
    		color = ChatColor.GRAY;
    		name = "Default";
    		
    	}
    	
    	return color + "" + ChatColor.BOLD + name;
    }
    /*public static ChatColor getRankColor(String rank) {
    	rank = rank.toLowerCase();
    	
    	ChatColor color;
    	switch (rank) {
    	
    	case "vip":
    		color = ChatColor.LIGHT_PURPLE;
    		break;
    	case "mvp":
    		color = ChatColor.AQUA;
    		break;
    	case "youtube":
    		color = ChatColor.WHITE;
    	case "helper":
    		color = ChatColor.GREEN;
    		break;
    	case "mod":
    		color = ChatColor.YELLOW;
    		break;
    	case "admin":
    		color = ChatColor.RED;
    		break;
    	case "head-admin":
    		color = ChatColor.DARK_RED;
    		break;
    	case "owner":
    		color = ChatColor.GOLD;
    		break;
    	default:
    		color = ChatColor.GRAY;
    		
    	}
    	
    	return color;
    }*/
    
    public static String capFirst(String string) {
    	StringBuilder sb = new StringBuilder();
    	
		for (char c : string.toCharArray()) {
			sb.append(c);
		}
		
		char firstLetter = sb.charAt(0);
		String typeLetter = String.valueOf(firstLetter);
		sb.replace(0, 1, typeLetter.toUpperCase());
		
		String finalString = sb.toString();
		return finalString;
    }
    
    public static String underscoreToSpace(String string) {
    	String[] words = string.split("_");
    	if (words.length < 1) return null;
    	String sepString = "";
    	for (String word : words) {
    		sepString += capFirst(word) + " ";
    	}
    	
    	StringBuilder sb = new StringBuilder();
    	for (char c: sepString.toCharArray()) {
    		sb.append(c);
    	}
    	sb.deleteCharAt(sb.length() - 1);
    	
    	return sb.toString();
    }
    
}