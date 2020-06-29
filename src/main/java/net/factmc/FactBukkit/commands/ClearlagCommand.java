package net.factmc.FactBukkit.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import net.factmc.FactBukkit.listeners.ClearLagTask;

public class ClearlagCommand implements CommandExecutor, TabCompleter {
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equals("clearlag")) {
			
			if (args.length > 0) {
				
				if (args[0].equalsIgnoreCase("cancel")) {
					
					boolean result = ClearLagTask.getInstance().cancelNext();
					if (result)
						sender.sendMessage(ChatColor.GREEN + "The next clear has been cancelled");
					else
						sender.sendMessage(ChatColor.GREEN + "The next clear has already been cancelled");
					return true;
					
				}
				
				else if (args[0].equalsIgnoreCase("execute") && sender.hasPermission("factbukkit.clearlag.execute")) {
					
					int removed = ClearLagTask.getInstance().execute();
					sender.sendMessage(ChatColor.GREEN + "" + removed + " entities were removed");
					return true;
					
				}
				
			}
			
			String help = "<cancel";
			if (sender.hasPermission("factbukkit.clearlag.execute")) help += "|execute";
			help += ">";
			
			sender.sendMessage(ChatColor.RED + "Usage: /" + label + " " + help);
			return false;
			
		}
		
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> help = new ArrayList<String>();
		help.add("cancel");
		if (sender.hasPermission("factbukkit.clearlag.execute")) help.add("execute");
		
		if (args.length > 0) {
			if (args.length > 1) return new ArrayList<String>();
			
			filter(help, args[0].toLowerCase());
		}
		
		return help;
		
	}
	
	private void filter(List<String> list, String start) {
		for (String string : new ArrayList<String>(list)) {
			if (!string.startsWith(start)) list.remove(string);
		}
	}
	
}