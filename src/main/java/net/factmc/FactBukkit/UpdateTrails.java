package net.factmc.FactBukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import de.myzelyam.api.vanish.VanishAPI;
import net.factmc.FactCore.FactSQLConnector;

public class UpdateTrails implements Runnable, Listener {
	
	private static List<String[]> particleData = new ArrayList<String[]>();
	
	private static void copyData() {
		particleData.clear();
		for (Player player : Bukkit.getOnlinePlayers()) {
			UUID uuid = player.getUniqueId();
			String[] data = FactSQLConnector.getStringValue(FactSQLConnector.getOptionsTable(), uuid, "TRAIL", "CLOAK");
			
			particleData.add(new String[] {uuid.toString(), data[0], data[1]});
		}
	}
	
	private static String[] getArray(UUID uuid) {
		for (String[] array : particleData) {
			UUID check = UUID.fromString(array[0]);
			if (check.equals(uuid)) {
				return array;
			}
		}
		return null;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		copyData();
	}
	
	

    // Main class for bukkit scheduling
    private static JavaPlugin plugin;
    
    // Our scheduled task's assigned id,needed for canceling
    private static Integer assignedTaskId;
    
    public static void start(JavaPlugin plugin) {
        // Initializing fields
        UpdateTrails.plugin = plugin;
        copyData();
        schedule();
    }
    
    private int tick;
    private int yaw = 0;
    
    private int reload = 0;
    
    public void run() {
    	if (reload > 39) { copyData(); reload = 0; }
    	else reload++;
    	
    	for (Player player : Bukkit.getOnlinePlayers()) {
    		if (player.getGameMode() != GameMode.SPECTATOR && !(Main.useVanish && VanishAPI.isInvisible(player))) {
    			String[] array = getArray(player.getUniqueId());
	    		String trail = array[1];
	    		if (!trail.equalsIgnoreCase("NONE")) {
		    		try {
		    			Particle particle = trail.equals("FACTMC") ? Particle.REDSTONE : Particle.valueOf(trail.toUpperCase());
		    			
		    			double correction = 0;
		    			if (particle == Particle.DRIP_LAVA || particle == Particle.DRIP_WATER || particle == Particle.VILLAGER_HAPPY) {
		    				correction = 0.088;
		    			}
		    			
		    			Location playerLoc = player.getLocation().add(0, 2.2, 0);
		    			if (particle == Particle.PORTAL) playerLoc.add(0, -1, 0);
		    			
		            	playerLoc.setPitch(0);
		            	playerLoc.setYaw(yaw);
		            	Vector vec = playerLoc.getDirection().multiply(0.35);
		            	Location baseLoc = playerLoc.add(vec);
		            	baseLoc.add(correction, 0, correction);
		            	
		            	Object data = null;
		            	if (particle == Particle.FALLING_DUST) data = Bukkit.createBlockData(Material.GRAVEL);
		            	else if (trail.equals("FACTMC")) data = new DustOptions(yaw % 20 == 0 ? Color.fromRGB(32,162,17) : Color.fromRGB(255,236,0), 1);
		            	else if (particle == Particle.REDSTONE) data = new DustOptions(randomColor(), 1);
		            	
		            	for (Player p : Bukkit.getOnlinePlayers()) {
		            		if (p.canSee(player))
		            				p.spawnParticle(particle, baseLoc, 1, data);
		            	}
		    			
		    		} catch (IllegalArgumentException e) {
		    			Main.getPlugin().getLogger().warning("The particle trail for " + player.getName() + ": '" + trail + "' is not valid");
		    			FactSQLConnector.getStringValue(FactSQLConnector.getOptionsTable(), player.getUniqueId(), "TRAIL", "NONE");
		    		}
	    		}
    		}
    	}
    	
    	tick++;
    	if (tick > 40) tick = 1;
    	
    	if (yaw >= 350) yaw = 0;
    	else yaw += 10;
    	
    }
    
    public static Color randomColor() {
    	
    	List<Integer> rgb = new ArrayList<Integer>();
    	for (int i = 0; i < 3; i++) {
    		int rand = (int) (Math.random() * 256);
    		rgb.add(rand);
    	}
    	
    	return Color.fromRGB(rgb.get(0), rgb.get(1), rgb.get(2));
    	
    }
    
    
    public static void end() {
    	if (assignedTaskId != null) Bukkit.getScheduler().cancelTask(assignedTaskId);
    	UpdateTrails.plugin = null;
    }

    /**
     * Schedules this instance to "run" every second
     */
    public static void schedule() {
        // Initialize our assigned task's id, for later use so we can cancel
        assignedTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new UpdateTrails(), 10L, 1L);
    }

}