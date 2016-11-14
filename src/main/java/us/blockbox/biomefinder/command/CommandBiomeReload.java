package us.blockbox.biomefinder.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import us.blockbox.biomefinder.BfConfig;
import us.blockbox.biomefinder.CacheBuilder;
import us.blockbox.biomefinder.locale.BfLocale;
import us.blockbox.biomefinder.locale.BfMessage;

import java.util.logging.Logger;

import static us.blockbox.biomefinder.BfConfig.*;
import static us.blockbox.biomefinder.BiomeFinder.prefix;

//Created 11/10/2016 1:48 AM
public class CommandBiomeReload implements CommandExecutor{

	private final Logger log;
	private BfLocale locale = BfConfig.getLocale();

	public CommandBiomeReload(JavaPlugin plugin){
		log = plugin.getLogger();
	}

	@Override
	public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args){
		if(!sender.hasPermission("biomefinder.reload")){
			sender.sendMessage(locale.getMessage(BfMessage.PLAYER_NO_PERMISSION));
			return true;
		}

		if(CacheBuilder.cacheBuildRunning){
			log.info("A cache is currently being built. Try again when it's finished.");
			return true;
		}

		saveBiomeCaches();
		loadBiomeCaches();
		loadConfig();
		sender.sendMessage(prefix + locale.getMessage(BfMessage.CONFIG_RELOADED));
		return true;
	}
}
