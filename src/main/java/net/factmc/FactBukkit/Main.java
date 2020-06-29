package net.factmc.FactBukkit;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import net.factmc.FactBukkit.commands.ClearlagCommand;
import net.factmc.FactBukkit.commands.PointsCommand;
import net.factmc.FactBukkit.commands.ReloadCommand;
import net.factmc.FactBukkit.commands.ServersCommand;
import net.factmc.FactBukkit.commands.SignEditCommand;
import net.factmc.FactBukkit.commands.StatsCommand;
import net.factmc.FactBukkit.commands.VoteCommand;
import net.factmc.FactBukkit.gui.ServerGUI;
import net.factmc.FactBukkit.gui.StatsGUI;
import net.factmc.FactBukkit.listeners.ClaimingShovelBlocker;
import net.factmc.FactBukkit.listeners.ClearLagTask;
import net.factmc.FactBukkit.listeners.LuckPermsEvents;
import net.factmc.FactBukkit.listeners.VanishEvents;
import net.factmc.FactCore.CoreUtils;
import net.factmc.FactCore.bukkit.CustomBossbar;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
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
    	saveDefaultConfig();
    	
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
    	
    	getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    	setupEconomy();
    	
    	/*
    	disableRankTag = getServer().getPluginManager().getPlugin("FactHub") != null;
    	if (disableRankTag) {
    		plugin.getLogger().info("Found FactHub. Disabling Listeners");
    	}
    	*/
    	
    	registerEvents();
    	registerCommands();
    	
    	loadBossBar();
    	plugin.getLogger().info("Loaded BossBar");
    	
    	/*RegisteredServiceProvider<Permission> permRSP = getServer().getServicesManager().getRegistration(Permission.class);
        perms = permRSP.getProvider();
        plugin.getLogger().info("Connected to " + perms.getName());*/
    	
        if (!disableRankTag) {
        	sb = Bukkit.getScoreboardManager().getMainScoreboard();
	    	registerTeams(sb);
	        plugin.getLogger().info("Registered Teams");
        }
        
        if (getConfig().getBoolean("clearlag.enabled")) {
        	int interval = getConfig().getInt("clearlag.interval");
        	Integer[] warnings = getConfig().getIntegerList("clearlag.warnings").toArray(new Integer[0]);
        	boolean hideMessages = getConfig().getBoolean("clearlag.hide-messages");
        	Class<?>[] removeTypes = ClearLagTask.getTypeClasses();
        	new ClearLagTask(this, interval, warnings, hideMessages, removeTypes);
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
    	getServer().getPluginManager().registerEvents(new ServerGUI(), plugin);
    	
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
    	getCommand("vote").setExecutor(new VoteCommand());
    	getCommand("servers").setExecutor(new ServersCommand());
    	getCommand("clearlag").setExecutor(new ClearlagCommand());
    	
    	SignEditCommand.load();
    }
    
    public void loadBossBar() {
    	List<String> titles = new ArrayList<String>();
    	for (String title : getConfig().getStringList("bossbar.title")) {
    		titles.add(ChatColor.translateAlternateColorCodes('&', title));
    	}
    	
    	List<BarColor> colors = new ArrayList<BarColor>();
    	for (String color : getConfig().getStringList("bossbar.color")) {
    		colors.add(BarColor.valueOf(color));
    	}
    	List<BarStyle> styles = new ArrayList<BarStyle>();
    	for (String style : getConfig().getStringList("bossbar.style")) {
    		styles.add(BarStyle.valueOf(style));
    	}
    	
    	int rate = (int) (getConfig().getDouble("bossbar.rate") * 20);
    	new CustomBossbar(plugin, titles, colors, styles, rate);
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
    	
		for (Group raw : LuckPermsProvider.get().getGroupManager().getLoadedGroups()) {
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