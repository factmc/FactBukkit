package net.factmc.FactBasic.supervanish;

import org.bukkit.entity.Player;

public class SendPause {
	
	protected Player player;
	protected String msg;
	
	public SendPause(Player player, String msg) {
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
