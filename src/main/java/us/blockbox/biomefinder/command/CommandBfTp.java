package us.blockbox.biomefinder.command;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.blockbox.biomefinder.BfConfig;
import us.blockbox.biomefinder.BiomeFinder;
import us.blockbox.biomefinder.CacheManager;
import us.blockbox.biomefinder.locale.BfLocale;
import us.blockbox.biomefinder.locale.BfMessage;
import us.blockbox.uilib.UIPlugin;
import us.blockbox.uilib.component.CommandItem;
import us.blockbox.uilib.api.Component;
import us.blockbox.uilib.view.InventoryView;
import us.blockbox.uilib.api.View;

import java.util.*;

import static org.bukkit.block.Biome.*;
import static us.blockbox.biomefinder.BiomeFinder.*;

public class CommandBfTp implements CommandExecutor{

	private final BfConfig bfc = BiomeFinder.getPlugin().getBfConfig();
	private final BfLocale locale = bfc.getLocale();
	private final CacheManager cacheManager = BiomeFinder.getPlugin().getCacheManager();
	private final EnumMap<Biome,ItemStack> guiStacks;

	{
		if(BiomeFinder.getPlugin().isUiLibEnabled()){
			guiStacks = getGuiStacks();
		}else{
			guiStacks = null;
		}
	}

	@Override
	public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args){
		if(!sender.hasPermission("biomefinder.tp")){
			sender.sendMessage(prefix + locale.getMessage(BfMessage.PLAYER_NO_PERMISSION));
			return true;
		}
		final Player target = getTarget(sender,args);
		if(target == null){
			//any error message was sent in getTarget, just return
			return true;
		}
		if(sender != target && !sender.hasPermission("biomefinder.tp.other")){
			sender.sendMessage(locale.getMessage(BfMessage.PLAYER_NO_PERMISSION));
			return true;
		}
		final World world = target.getWorld();
		if(!cacheManager.hasCache(world)){
			sender.sendMessage(prefix + locale.getMessage(BfMessage.WORLD_INDEX_MISSING));
			return true;
		}
		if(args.length < 1){
			//don't need to consider if the sender isn't the target here
			//you need at least 2 args to have a sender other than yourself
			if(BiomeFinder.getPlugin().isUiLibEnabled()){
				showSelectionUI(target,world);
			}else{
				sender.sendMessage(prefix + locale.getMessage(BfMessage.BIOME_NAME_UNSPECIFIED));
			}
			return true;
		}
		final Biome b = parseBiome(args[0]);
		if(b == null){
			sender.sendMessage(prefix + locale.getMessage(BfMessage.BIOME_NAME_UNSPECIFIED));
		}else{
			final LocationPreference pref = getLocationPreference(args);
			tpToBiome(sender,target,b,pref);
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	private Player getTarget(CommandSender sender,String[] args){
		Player target = null;
		int argsLength = args.length;
		if(argsLength >= 2){
			//we have biome name and player name at least
			String name = args[argsLength - 1];//last argument should be player name
			Player byName = Bukkit.getPlayer(name);
			if(byName == null){
				if(argsLength > 2){
					//we know there was a keyword so the last arg is definitely the player name
					//player wasn't found by name so return
					sender.sendMessage(BfLocale.format(prefix + String.format(locale.getMessage(BfMessage.PLAYER_NOT_FOUND),name),!bfc.isLogColorEnabled()));
					return null;
				}
				//keep going with the assumption that the last arg is a keyword, not a player name
			}else{
				target = byName;
			}
		}
		if(target == null){
			if(sender instanceof Player){
				target = ((Player)sender);
			}else{
				sender.sendMessage(BfLocale.format(prefix + locale.getMessage(BfMessage.COMMAND_NOT_PLAYER),!bfc.isLogColorEnabled()));
				return null;
			}
		}
		return target;
	}

	private static LocationPreference getLocationPreference(String[] args){
		if(args.length >= 2){
			final String distanceArg = args[1].toLowerCase();
			if(distanceArg.startsWith("near")){
				return LocationPreference.NEAR;
			}else if(distanceArg.startsWith("far")){
				return LocationPreference.FAR;
			}
		}
		return LocationPreference.ANY;
	}

	private void showSelectionUI(Player p,World world){
		final List<Biome> biomes = new ArrayList<>(cacheManager.getCache(world).keySet());
		Collections.sort(biomes,new Comparator<Biome>(){
			@Override
			public int compare(Biome o1,Biome o2){
				return o1.name().compareTo(o2.name()); //todo use names from locale
			}
		});
		final Component[] components = new Component[biomes.size()];
		int i = 0;
		for(final Biome biome : biomes){
			String biomeName = biome.name();
			ItemStack biomeStack = guiStacks.get(biome);
			ItemStack itemStack = biomeStack == null ? new ItemStack(Material.SAPLING) : biomeStack.clone();
			final String name = locale.getFriendlyName(biome);
			ItemMeta itemMeta = itemStack.getItemMeta();
			itemMeta.setDisplayName(name);
			itemStack.setItemMeta(itemMeta);
			components[i++] = new CommandItem(name,biomeName.toLowerCase(),itemStack,"bftp " + biomeName);
		}
		View v = InventoryView.createPaginated("Biome Selector",components,4);
		UIPlugin.getViewManager().setView(p,v);
	}

	private static EnumMap<Biome,ItemStack> getGuiStacks(){
		final EnumMap<Biome,ItemStack> m = new EnumMap<>(Biome.class);
		putMulti(m,new ItemStack(Material.GRASS,1),
				PLAINS,MUTATED_PLAINS);
		putMulti(m,new ItemStack(Material.SAPLING,1),
				FOREST,FOREST_HILLS,MUTATED_FOREST);
		putMulti(m,new ItemStack(Material.SAPLING,1,((short)2)),
				BIRCH_FOREST,BIRCH_FOREST_HILLS,MUTATED_BIRCH_FOREST,MUTATED_BIRCH_FOREST_HILLS);
		putMulti(m,new ItemStack(Material.SAND,1),
				DESERT,DESERT_HILLS,MUTATED_DESERT,BEACHES);
		putMulti(m,new ItemStack(Material.SAPLING,1,((short)4)),
				SAVANNA,SAVANNA_ROCK,MUTATED_SAVANNA,MUTATED_SAVANNA_ROCK);
		putMulti(m,new ItemStack(Material.SAPLING,1,((short)1)),
				EXTREME_HILLS_WITH_TREES,MUTATED_EXTREME_HILLS_WITH_TREES,TAIGA,TAIGA_HILLS,MUTATED_REDWOOD_TAIGA,MUTATED_TAIGA,REDWOOD_TAIGA,MUTATED_REDWOOD_TAIGA_HILLS,REDWOOD_TAIGA_HILLS);
		putMulti(m,new ItemStack(Material.SNOW_BLOCK,1),
				COLD_BEACH,TAIGA_COLD,TAIGA_COLD_HILLS,MUTATED_TAIGA_COLD);
		putMulti(m,new ItemStack(Material.ICE,1),
				ICE_FLATS,ICE_MOUNTAINS,MUTATED_ICE_FLATS,FROZEN_OCEAN,FROZEN_RIVER);
		putMulti(m,new ItemStack(Material.WATER_BUCKET,1),
				OCEAN,RIVER,DEEP_OCEAN);
		putMulti(m,new ItemStack(Material.RED_MUSHROOM,1),
				MUSHROOM_ISLAND,MUSHROOM_ISLAND_SHORE);
		putMulti(m,new ItemStack(Material.SAPLING,1,(short)3),
				JUNGLE,JUNGLE_EDGE,JUNGLE_HILLS,MUTATED_JUNGLE,MUTATED_JUNGLE_EDGE);
		putMulti(m,new ItemStack(Material.STAINED_CLAY,1,(short)1),
				MESA,MESA_CLEAR_ROCK,MESA_ROCK,MUTATED_MESA,MUTATED_MESA_CLEAR_ROCK,MUTATED_MESA_ROCK);
		putMulti(m,new ItemStack(Material.STONE,1),
				EXTREME_HILLS,MUTATED_EXTREME_HILLS,SMALLER_EXTREME_HILLS,STONE_BEACH);
		putMulti(m,new ItemStack(Material.SAPLING,1,(short)5),
				ROOFED_FOREST,MUTATED_ROOFED_FOREST);
		putMulti(m,new ItemStack(Material.NETHERRACK),
				HELL);
		putMulti(m,new ItemStack(Material.COAL_BLOCK),
				SKY);
		putMulti(m,new ItemStack(Material.COAL_BLOCK),
				VOID);
		putMulti(m,new ItemStack(Material.SLIME_BLOCK,1),
				SWAMPLAND,MUTATED_SWAMPLAND);
		printMissing(m);
		return m;
	}

	private static void printMissing(EnumMap<Biome,ItemStack> m){
		final List<Biome> missing = new ArrayList<>();
		for(final Biome b : Biome.values()){
			if(!m.containsKey(b)){
				missing.add(b);
			}
		}
		if(!missing.isEmpty()){
			final StringBuilder sb = new StringBuilder("The following biomes don't have a corresponding material for the selection GUI: ");
			for(final Biome biome : missing){
				sb.append(biome.name()).append(' ');
			}
			BiomeFinder.getPlugin().getConsole().warn(sb.toString());
		}
	}

	private static <V,K> void putMulti(Map<K,V> map,V v,K... ks){
		for(final K k : ks){
			map.put(k,v);
		}
	}
}
