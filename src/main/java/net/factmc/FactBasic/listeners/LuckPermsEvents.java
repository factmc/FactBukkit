package net.factmc.FactBasic.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.event.EventBus;
import me.lucko.luckperms.api.event.user.track.UserDemoteEvent;
import me.lucko.luckperms.api.event.user.track.UserPromoteEvent;
import net.factmc.FactBasic.JoinEvents;
import net.factmc.FactBasic.Main;

public class LuckPermsEvents {
	
	public LuckPermsEvents() {
        EventBus eventBus = LuckPerms.getApi().getEventBus();
        eventBus.subscribe(UserPromoteEvent.class, this::userPromoted);
        eventBus.subscribe(UserDemoteEvent.class, this::userDemoted);
    }
	
	public void userPromoted(UserPromoteEvent event) {
		rankChange(event.getUser().getUuid());
	}
	public void userDemoted(UserDemoteEvent event) {
		rankChange(event.getUser().getUuid());
	}
	
	public void rankChange(UUID uuid) {
    	
		Player player = Bukkit.getPlayer(uuid);
		if (player != null)
			JoinEvents.updateTeam(player, Main.getScoreboard());
		
	}
	
}