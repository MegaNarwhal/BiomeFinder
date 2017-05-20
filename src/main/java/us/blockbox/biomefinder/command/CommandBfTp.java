package us.blockbox.biomefinder.command;

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
import us.blockbox.uilib.component.Component;
import us.blockbox.uilib.view.InventoryView;
import us.blockbox.uilib.view.View;

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
			guiStacks = new EnumMap<>(Biome.class);
			for(final Biome b : new Biome[]{PLAINS,MUTATED_PLAINS}){
				guiStacks.put(b,new ItemStack(Material.GRASS,1));
			}
			for(final Biome b : new Biome[]{FOREST,FOREST_HILLS,MUTATED_FOREST}){
				guiStacks.put(b,new ItemStack(Material.SAPLING,1));
			}
			for(final Biome b : new Biome[]{BIRCH_FOREST,BIRCH_FOREST_HILLS,MUTATED_BIRCH_FOREST,MUTATED_BIRCH_FOREST_HILLS}){
				guiStacks.put(b,new ItemStack(Material.SAPLING,1,((short)2)));
			}
			for(final Biome b : new Biome[]{DESERT,DESERT_HILLS,MUTATED_DESERT,BEACHES}){
				guiStacks.put(b,new ItemStack(Material.SAND,1));
			}
			for(final Biome b : new Biome[]{SAVANNA,SAVANNA_ROCK,MUTATED_SAVANNA,MUTATED_SAVANNA_ROCK}){
				guiStacks.put(b,new ItemStack(Material.SAPLING,1,((short)4)));
			}
			for(final Biome b : new Biome[]{EXTREME_HILLS_WITH_TREES,MUTATED_EXTREME_HILLS_WITH_TREES,TAIGA,TAIGA_HILLS,MUTATED_REDWOOD_TAIGA,MUTATED_TAIGA,REDWOOD_TAIGA,MUTATED_REDWOOD_TAIGA_HILLS,REDWOOD_TAIGA_HILLS}){
				guiStacks.put(b,new ItemStack(Material.SAPLING,1,((short)1)));
			}
			for(final Biome b : new Biome[]{COLD_BEACH,TAIGA_COLD,TAIGA_COLD_HILLS,MUTATED_TAIGA_COLD}){
				guiStacks.put(b,new ItemStack(Material.SNOW_BLOCK,1));
			}
			for(final Biome b : new Biome[]{ICE_FLATS,ICE_MOUNTAINS,MUTATED_ICE_FLATS,FROZEN_OCEAN,FROZEN_RIVER}){
				guiStacks.put(b,new ItemStack(Material.ICE,1));
			}
			for(final Biome b : new Biome[]{OCEAN,RIVER,DEEP_OCEAN}){
				guiStacks.put(b,new ItemStack(Material.WATER_BUCKET,1));
			}
			for(final Biome b : new Biome[]{MUSHROOM_ISLAND,MUSHROOM_ISLAND_SHORE}){
				guiStacks.put(b,new ItemStack(Material.RED_MUSHROOM,1));
			}
			for(final Biome b : new Biome[]{JUNGLE,JUNGLE_EDGE,JUNGLE_HILLS,MUTATED_JUNGLE,MUTATED_JUNGLE_EDGE}){
				guiStacks.put(b,new ItemStack(Material.SAPLING,1,(short)3));
			}
			for(final Biome b : new Biome[]{MESA,MESA_CLEAR_ROCK,MESA_ROCK,MUTATED_MESA,MUTATED_MESA_CLEAR_ROCK,MUTATED_MESA_ROCK}){
				guiStacks.put(b,new ItemStack(Material.STAINED_CLAY,1,(short)1));
			}
			for(final Biome b : new Biome[]{EXTREME_HILLS,MUTATED_EXTREME_HILLS,SMALLER_EXTREME_HILLS,STONE_BEACH}){
				guiStacks.put(b,new ItemStack(Material.STONE,1));
			}
			for(final Biome b : new Biome[]{ROOFED_FOREST,MUTATED_ROOFED_FOREST}){
				guiStacks.put(b,new ItemStack(Material.SAPLING,1,(short)5));
			}
			guiStacks.put(HELL,new ItemStack(Material.NETHERRACK));
			guiStacks.put(SKY,new ItemStack(Material.COAL_BLOCK));
			guiStacks.put(VOID,new ItemStack(Material.COAL_BLOCK));
			for(final Biome b : new Biome[]{SWAMPLAND,MUTATED_SWAMPLAND}){
				guiStacks.put(b,new ItemStack(Material.SLIME_BLOCK,1));
			}
			final List<Biome> missing = new ArrayList<>();
			for(final Biome b : values()){
				if(!guiStacks.containsKey(b)){
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
		}else{
			guiStacks = null;
		}
	}

	@Override
	public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args){
		if(!(sender instanceof Player)){
			sender.sendMessage(BfLocale.format(prefix + locale.getMessage(BfMessage.COMMAND_NOT_PLAYER),!bfc.isLogColorEnabled()));
			return true;
		}
		if(!sender.hasPermission("biomefinder.tp")){
			sender.sendMessage(prefix + locale.getMessage(BfMessage.PLAYER_NO_PERMISSION));
			return true;
		}
		final Player p = (Player)sender;
		final World world = p.getWorld();
		if(!cacheManager.hasCache(world)){
			sender.sendMessage(prefix + locale.getMessage(BfMessage.WORLD_INDEX_MISSING));
			return true;
		}
		if(args.length < 1){
			if(BiomeFinder.getPlugin().isUiLibEnabled()){ //todo uilib pagination
				showSelectionUI(p,world);
			}else{
				sender.sendMessage(prefix + locale.getMessage(BfMessage.BIOME_NAME_UNSPECIFIED));
			}
			return true;
		}
		Biome b = parseBiome(args[0]);
		if(b == null){
			sender.sendMessage(prefix + locale.getMessage(BfMessage.BIOME_NAME_UNSPECIFIED));
			return true;
		}
		if(args.length >= 2){
			if(args[1].toLowerCase().startsWith("near")){
				tpToBiome(p,b,true);
				return true;
			}
		}
		tpToBiome(p,b);
		return true;
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
}