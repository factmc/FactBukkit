package net.factmc.FactBukkit.listeners;

import org.bukkit.entity.Player;

public class VanishSendPause {
	
	protected Player player;
	protected String msg;
	
	public VanishSendPause(Player player, String msg) {
		this.player = player;
		this.msg = msg;
	}
	
	
	public Player getPlayer() {
		return this.player;
	}
	
	public String getMessage() {
		return this.msg;
	}
	
	
	
	@Override
	public String toString() {
		
		return this.player.getName() + ", " + this.msg;
		
	}

}
