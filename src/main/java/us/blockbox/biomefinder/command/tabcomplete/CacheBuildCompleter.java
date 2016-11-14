package us.blockbox.biomefinder.command.tabcomplete;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//Created 11/11/2016 3:48 AM
public class CacheBuildCompleter implements TabCompleter{
	@Override
	public List<String> onTabComplete(CommandSender sender,Command cmd,String alias,String[] args){
		if(!(sender instanceof ConsoleCommandSender)){
			return Collections.emptyList();
		}
		if(args.length == 1){
			List<String> worlds = new ArrayList<>();
			for(World w : Bukkit.getServer().getWorlds()){
				if(w.getName().toLowerCase().startsWith(args[0].toLowerCase())){
					worlds.add(w.getName());
				}
			}
			return worlds;
		}
		if(args.length == 2){
			return Collections.singletonList("spawn");
		}
		return Collections.emptyList();
	}
}