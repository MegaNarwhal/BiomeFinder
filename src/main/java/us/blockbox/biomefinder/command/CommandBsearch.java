package us.blockbox.biomefinder.command;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.blockbox.biomefinder.BfConfig;
import us.blockbox.biomefinder.BiomeNearbySearcher;
import us.blockbox.biomefinder.Coord;
import us.blockbox.biomefinder.locale.BfLocale;

import java.text.DecimalFormat;
import java.util.Map;

import static us.blockbox.biomefinder.BiomeFinder.hasCache;
import static us.blockbox.biomefinder.BiomeFinder.prefix;
import static us.blockbox.biomefinder.locale.BfMessage.*;

//Created 11/10/2016 12:32 AM
public class CommandBsearch implements CommandExecutor{

	private static final DecimalFormat format = new DecimalFormat("0.#");

	private final JavaPlugin plugin;
	private static final BfLocale locale = BfConfig.getInstance().getLocale();

	public CommandBsearch(JavaPlugin plugin){
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender,Command command,String s,String[] strings){
		if(!sender.hasPermission("biomefinder.bsearch")){
			sender.sendMessage(prefix + locale.getMessage(PLAYER_NO_PERMISSION));
			return true;
		}
		if(!(sender instanceof Player)){
			sender.sendMessage(prefix + locale.getMessage(COMMAND_NOT_PLAYER));
			return true;
		}
		final Player p = (Player)sender;
		if(!hasCache(p.getWorld())){
			sender.sendMessage(locale.getMessage(WORLD_INDEX_MISSING));
			return true;
		}
		final Location pLoc = p.getLocation();
		final BiomeNearbySearcher searcher = new BiomeNearbySearcher(pLoc);
		final Map<Biome,Coord> results = searcher.search();
		final Coord pCoord = new Coord(pLoc);
		sender.sendMessage(locale.getMessage(NEARBY_HEADER));
		new BukkitRunnable(){
			@Override
			public void run(){
				for(Map.Entry<Biome,Coord> c : results.entrySet()){
					final String bName = ChatColor.GREEN + c.getKey().toString() + ": " + ChatColor.RESET;
					final Coord coord = c.getValue();
					final String coords = "X " + coord.x + ", Z " + coord.z + ChatColor.GRAY + " (Distance: " + format.format(pCoord.distance(coord)) + ")";
					p.sendMessage(bName + coords);
				}
			}
		}.runTaskAsynchronously(plugin);
		return true;
	}
}
