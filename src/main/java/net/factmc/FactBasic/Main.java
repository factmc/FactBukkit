package net.factmc.FactBasic;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import net.factmc.FactBasic.commands.ReloadCommand;
import net.factmc.FactBasic.supervanish.VanishEvents;
import net.milkbowl.vault.permission.Permission;

public class Main extends JavaPlugin implements Listener {
	
	public static JavaPlugin plugin;
	
	public static Permission perms;
	
	//public static List<Object> roles = new ArrayList<Object>();
	
	public static boolean useVanish = true;
	public static boolean disableRankTag = false;
	
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
    		plugin.getLogger().info("Found SuperVanish. Monitoring events");
    	}
    	
    	disableRankTag = getServer().getPluginManager().getPlugin("FactHub") != null;
    	if (disableRankTag) {
    		plugin.getLogger().info("Found FactHub. Disabling Listeners");
    	}
    	
    	Bukkit.getPluginCommand("rtag-update").setExecutor(new ReloadCommand());
    	registerEvents();
    	//registerCommands();
    	
    	RegisteredServiceProvider<Permission> permRSP = getServer().getServicesManager().getRegistration(Permission.class);
        perms = permRSP.getProvider();
        plugin.getLogger().info("Connected to " + perms.getName());
    	
        if (!disableRankTag) {
	    	registerTeams(Bukkit.getScoreboardManager().getMainScoreboard());
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
    		
    		Bukkit.getServer().getPluginManager()
    				.registerEvents(new VanishEvents(), plugin);
    		
    	}
    	
    	if (!disableRankTag) {
	    	
	    	Bukkit.getServer().getPluginManager()
	    			.registerEvents(new JoinEvents(), plugin);
	    	
    	}
    	
    }
    
    public void registerCommands() {
    	//plugin.getCommand("cmd").setExecutor(this);
    }
    
    public static JavaPlugin getPlugin() {
        return plugin;
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
    	
		for (String group : perms.getGroups()) {
        	if (!group.equalsIgnoreCase("default")) {
        		Team oldTeam = sb.getTeam("rank-" + group);
        		if (oldTeam != null) oldTeam.unregister();
	        	Team team = sb.registerNewTeam("rank-" + group);
	        	String name = getColoredRank(group);
	        	String prefix = "[" + name + ChatColor.RESET + "]";
	        	team.setPrefix(prefix);
	        	//team.setColor(getRankColor(group));
        	}
        }
    	
    }
    
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
    		name = "Hd Admin";
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