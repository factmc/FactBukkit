package net.factmc.FactBukkit.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.factmc.FactBukkit.Main;
import net.md_5.bungee.api.ChatColor;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaAPI;

public class VersionChecker implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {

		@SuppressWarnings("unchecked")
		ViaAPI<Player> via = Via.getAPI();
		int playerProtcol = via.getPlayerVersion(event.getPlayer());

		if (playerProtcol != Main.serverProtocol) {
			event.getPlayer().sendMessage(
					ChatColor.RED + "You are not using a supported version, you may not be able to join some servers");
		}

	}

}
