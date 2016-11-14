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
import us.blockbox.biomefinder.BiomeNearbySearcher;
import us.blockbox.biomefinder.Coord;

import java.text.DecimalFormat;
import java.util.Map;

import static us.blockbox.biomefinder.BiomeFinder.biomeCache;
import static us.blockbox.biomefinder.BiomeFinder.prefix;

//Created 11/10/2016 12:32 AM
public class CommandBsearch implements CommandExecutor{

	private static final DecimalFormat format = new DecimalFormat("0.#");

	private final JavaPlugin plugin;

	public CommandBsearch(JavaPlugin plugin){
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender,Command command,String s,String[] strings){
		if(!sender.hasPermission("biomefinder.bsearch")){
			sender.sendMessage(ChatColor.DARK_RED + prefix + "You do not have permission.");
			return true;
		}
		if(!(sender instanceof Player)){
			sender.sendMessage(prefix + "You must be a player to use this command.");
			return true;
		}
		final Player p = (Player)sender;
		if(!biomeCache.containsKey(p.getWorld())){
			sender.sendMessage(ChatColor.GRAY + "This world's biomes have not been indexed yet.");
			return true;
		}
		Location pLoc = p.getLocation();
		BiomeNearbySearcher searcher = new BiomeNearbySearcher(pLoc);
		final Map<Biome,Coord> results = searcher.search();
		final Coord pCoord = new Coord(pLoc);
		sender.sendMessage(ChatColor.GREEN + "======| Nearby Biomes |======");
		new BukkitRunnable(){
			@Override
			public void run(){
				for(Map.Entry<Biome,Coord> c : results.entrySet()){
					final String bName = ChatColor.GREEN + c.getKey().toString() + ": " + ChatColor.RESET;
					Coord coord = c.getValue();
					String coords = "X " + coord.x + ", Z " + coord.z + ChatColor.GRAY + " (Distance: " + format.format(pCoord.distance(coord)) + ")";
					p.sendMessage(bName + coords);
				}
			}
		}.runTaskAsynchronously(plugin);
		return true;
	}
}
