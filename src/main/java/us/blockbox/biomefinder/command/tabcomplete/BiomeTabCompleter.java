package us.blockbox.biomefinder.command.tabcomplete;

import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

public class BiomeTabCompleter implements TabCompleter{

	private static final List<String> biomes;
	private static final List<String> keywords = Arrays.asList("near","far");

	static{ //todo use names from locale too
		final Biome[] values = Biome.values();
		biomes = new ArrayList<>(values.length);
		for(final Biome b : values){
			biomes.add(b.name());
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender commandSender,Command command,String alias,String[] args){
		Locale stringLocale = Locale.US;
		if(args.length == 1 && !args[0].isEmpty()){
			final List<String> tabBiomes = new ArrayList<>();
			for(String b : biomes){
				if(b.toLowerCase(stringLocale).startsWith(args[0].toLowerCase(stringLocale))){
					tabBiomes.add(b);
				}
			}
			return tabBiomes;
		}
		if(args.length == 2 && command.getName().equalsIgnoreCase("bftp")){
			if(args[1].isEmpty()){
				return keywords;
			}
			final String k = args[1].toLowerCase(stringLocale);
			for(final String keyword : keywords){
				if(keyword.startsWith(k)){
					return Collections.singletonList(keyword);
				}
			}
		}
		return null;
	}
}
