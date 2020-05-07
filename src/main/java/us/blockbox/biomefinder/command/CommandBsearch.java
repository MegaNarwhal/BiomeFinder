package us.blockbox.biomefinder.command;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import us.blockbox.biomefinder.BfConfig;
import us.blockbox.biomefinder.BiomeFinder;
import us.blockbox.biomefinder.BiomeNearbySearcher;
import us.blockbox.biomefinder.Coord;
import us.blockbox.biomefinder.api.CacheManager;
import us.blockbox.biomefinder.api.LocaleManager;
import us.blockbox.biomefinder.locale.BfLocale;

import java.text.DecimalFormat;
import java.util.Map;

import static us.blockbox.biomefinder.locale.BfMessage.*;

public class CommandBsearch implements CommandExecutor{
	private final BiomeFinder plugin;
	private final BfConfig bfc;
	private final LocaleManager lm;
	private final CacheManager cacheManager;

	public CommandBsearch(BiomeFinder plugin,BfConfig bfc,LocaleManager lm,CacheManager cacheManager){
		this.plugin = plugin;
		this.bfc = bfc;
		this.lm = lm;
		this.cacheManager = cacheManager;
	}

	@Override
	public boolean onCommand(CommandSender sender,Command command,String s,String[] strings){
		if(!(sender instanceof Player)){
			final String message = BfLocale.format(lm.getPrefix() + lm.get(COMMAND_NOT_PLAYER),!bfc.isLogColorEnabled());
			sender.sendMessage(message);
			return true;
		}
		if(!sender.hasPermission("biomefinder.bsearch")){
			sender.sendMessage(lm.getPrefix() + lm.get(PLAYER_NO_PERMISSION));
			return true;
		}
		final Player p = (Player)sender;
		if(!cacheManager.hasCache(p.getWorld())){
			sender.sendMessage(lm.get(WORLD_INDEX_MISSING));
			return true;
		}
		final Location pLoc = p.getLocation();
		final BiomeNearbySearcher searcher = new BiomeNearbySearcher(pLoc);
		final Map<Biome,Coord> results = searcher.search();
		final Coord pCoord = new Coord(pLoc);
		sender.sendMessage(lm.get(NEARBY_HEADER));
		new BukkitRunnable(){
			@Override
			public void run(){
				DecimalFormat format = new DecimalFormat("0.#");
				for(Map.Entry<Biome,Coord> c : results.entrySet()){
					final String bName = ChatColor.GREEN + c.getKey().toString() + ": " + ChatColor.RESET;
					final Coord coord = c.getValue();
					final String coords = "X " + coord.x + ", Z " + coord.z + ChatColor.GRAY + " (Distance: " + format.format(pCoord.distance(coord)) + ')';
					p.sendMessage(bName + coords);
				}
			}
		}.runTaskAsynchronously(plugin);
		return true;
	}
}
