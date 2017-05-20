package us.blockbox.biomefinder.command;

import com.onarandombox.MultiverseCore.api.MVPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import us.blockbox.biomefinder.BfConfig;
import us.blockbox.biomefinder.BiomeFinder;
import us.blockbox.biomefinder.CacheBuilder;
import us.blockbox.biomefinder.CacheManager;
import us.blockbox.biomefinder.event.CacheBuildStartEvent;
import us.blockbox.biomefinder.locale.BfLocale;

import java.util.logging.Logger;

import static us.blockbox.biomefinder.BiomeFinder.prefix;
import static us.blockbox.biomefinder.locale.BfMessage.*;

public class CommandBCacheBuild implements CommandExecutor{

	private final BiomeFinder plugin;
	private final Logger log;
	private final CacheManager cacheManager;
	private final BfConfig bfc;
	private final BfLocale locale;

	public CommandBCacheBuild(BiomeFinder plugin){
		this.plugin = plugin;
		log = plugin.getLogger();
		cacheManager = plugin.getCacheManager();
		bfc = plugin.getBfConfig();
		locale = bfc.getLocale();
	}


	@Override
	public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args){
		if(!(sender instanceof ConsoleCommandSender)){
			sender.sendMessage(prefix + locale.getMessage(COMMAND_NOT_CONSOLE));
			return true;
		}
		if(CacheBuilder.isCacheBuildRunning()){
			log.info(locale.getMessage(CACHE_BUILD_RUNNING));
			return true;
		}
		if(args.length < 1){
			log.info(locale.getMessage(WORLD_NAME_UNSPECIFIED));
			return false;
		}
		World world = Bukkit.getWorld(args[0]);
		if(world == null){
			log.info(locale.getMessage(WORLD_NAME_INVALID));
			return true;
		}
		if(cacheManager.hasCache(world) && bfc.getRecordedPoints(world) >= 512){
			log.info("World " + world.getName() + " was generated with \"points\" set to 512 or higher. If you want to regenerate it, remove the file and reload.");
			return true;
		}
		final int distance = bfc.getDistance();
		int centerX = 0;
		int centerZ = 0;
		if(args.length >= 2){
			if(args[1].equalsIgnoreCase("spawn")){
				final int sX;
				final int sZ;
				if(Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core") != null){
					log.info("Using Multiverse spawn point.");
					MVPlugin mv = (MVPlugin)Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
					Location mvSpawn = mv.getCore().getMVWorldManager().getMVWorld(world).getSpawnLocation();
					sX = mvSpawn.getBlockX();
					sZ = mvSpawn.getBlockZ();
				}else{
					log.info("Using vanilla spawn point.");
					sX = world.getSpawnLocation().getBlockX();
					sZ = world.getSpawnLocation().getBlockZ();
				}
				centerX = Math.round((float)sX/distance)*distance;
				centerZ = Math.round((float)sZ/distance)*distance;
			}else{
				try{
					centerX = Integer.valueOf(args[1]);
					centerZ = Integer.valueOf(args[2]);
				}catch(NumberFormatException ex){
					log.info("Invalid center coordinates specified.");
					return true;
				}
				centerX = Math.round((float)centerX/distance)*distance;
				centerZ = Math.round((float)centerZ/distance)*distance;
			}
		}

		CacheBuilder.startTime = System.currentTimeMillis();
		plugin.getServer().getPluginManager().callEvent(new CacheBuildStartEvent(world,centerX,centerZ));
		new CacheBuilder(plugin,world,centerX,centerZ).runTask(plugin);
		return true;
	}
}
