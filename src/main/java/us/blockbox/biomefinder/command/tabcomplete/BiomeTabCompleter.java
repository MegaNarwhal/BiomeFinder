package us.blockbox.biomefinder.command.tabcomplete;

import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BiomeTabCompleter implements TabCompleter{

	private static final List<String> biomes;

	static{ //todo use names from locale too
		biomes = new ArrayList<>();
		for(final Biome b : Biome.values()){
			biomes.add(b.name());
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender commandSender,Command command,String alias,String[] args){
		if(args.length == 1 && !args[0].isEmpty()){
			final List<String> tabBiomes = new ArrayList<>();
			for(String b : biomes){
				if(b.toLowerCase().startsWith(args[0].toLowerCase())){
					tabBiomes.add(b);
				}
			}
			return tabBiomes;
		}
		if(args.length == 2 & !args[0].equals("") && command.getName().equalsIgnoreCase("bftp")){
			return Collections.singletonList("near");
		}
		return Collections.emptyList();
	}
}