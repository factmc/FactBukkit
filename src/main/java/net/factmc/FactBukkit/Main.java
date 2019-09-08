package net.factmc.FactBukkit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.Group;
import net.factmc.FactBukkit.commands.ClearLagCancelCommand;
import net.factmc.FactBukkit.commands.PointsCommand;
import net.factmc.FactBukkit.commands.ReloadCommand;
import net.factmc.FactBukkit.commands.SignEditCommand;
import net.factmc.FactBukkit.commands.StatsCommand;
import net.factmc.FactBukkit.gui.StatsGUI;
import net.factmc.FactBukkit.listeners.ClaimingShovelBlocker;
import net.factmc.FactBukkit.listeners.LuckPermsEvents;
import net.factmc.FactBukkit.listeners.VanishEvents;
import net.factmc.FactCore.CoreUtils;

import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin implements Listener {
	
	public static JavaPlugin plugin;
	
	//public static List<Object> roles = new ArrayList<Object>();
	
	public static Economy econ = null;
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
    	
    	Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    	setupEconomy();
    	
    	/*
    	disableRankTag = getServer().getPluginManager().getPlugin("FactHub") != null;
    	if (disableRankTag) {
    		plugin.getLogger().info("Found FactHub. Disabling Listeners");
    	}
    	*/
    	
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
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    
    public void registerEvents() {
    	
    	getServer().getPluginManager().registerEvents(new StatsGUI(), plugin);
    	
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
	    	new LuckPermsEvents();
    	}
    	
    }
    
    public void registerCommands() {
    	//plugin.getCommand("cmd").setExecutor(this);
    	getCommand("stats").setExecutor(new StatsCommand());
    	getCommand("points").setExecutor(new PointsCommand());
    	getCommand("rtag-update").setExecutor(new ReloadCommand());
    	SignEditCommand.load();
    	
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
    	
		for (Group raw : LuckPerms.getApi().getGroups()) {
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
		        	String prefix = CoreUtils.getPrefix(group);
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
    
}