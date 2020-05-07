package us.blockbox.biomefinder.command;

import com.onarandombox.MultiverseCore.api.MVPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import us.blockbox.biomefinder.BfConfig;
import us.blockbox.biomefinder.CacheBuilder;
import us.blockbox.biomefinder.api.CacheManager;
import us.blockbox.biomefinder.api.LocaleManager;

import java.util.logging.Logger;

import static us.blockbox.biomefinder.locale.BfMessage.*;

public class CommandBCacheBuild implements CommandExecutor{
	private final Plugin plugin;
	private final Logger log;
	private final CacheManager cacheManager;
	private final BfConfig bfc;
	private final LocaleManager lm;

	public CommandBCacheBuild(Plugin plugin,Logger log,CacheManager cacheManager,BfConfig bfc,LocaleManager lm){
		this.plugin = plugin;
		this.log = log;
		this.cacheManager = cacheManager;
		this.bfc = bfc;
		this.lm = lm;
	}

	@Override
	public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args){
		if(CacheBuilder.isBuildRunning()){
			log.info(lm.get(CACHE_BUILD_RUNNING));
			return true;
		}
		if(args.length < 1){
			log.info(lm.get(WORLD_NAME_UNSPECIFIED));
			return false;
		}
		World world = Bukkit.getWorld(args[0]);
		if(world == null){
			log.info(lm.get(WORLD_NAME_INVALID));
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
				centerX = Math.round((float)sX / distance) * distance;
				centerZ = Math.round((float)sZ / distance) * distance;
			}else{
				try{
					centerX = Integer.valueOf(args[1]);
					centerZ = Integer.valueOf(args[2]);
				}catch(NumberFormatException ex){
					log.info("Invalid center coordinates specified.");
					return true;
				}
				centerX = Math.round((float)centerX / distance) * distance;
				centerZ = Math.round((float)centerZ / distance) * distance;
			}
		}
		new CacheBuilder(plugin,bfc,cacheManager,world,centerX,centerZ).start();
		return true;
	}
}
