package us.blockbox.biomefinder.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import us.blockbox.biomefinder.CacheBuilder;

import java.util.logging.Logger;

import static us.blockbox.biomefinder.BfConfig.loadBiomeCaches;
import static us.blockbox.biomefinder.BfConfig.loadConfig;
import static us.blockbox.biomefinder.BfConfig.saveBiomeCaches;
import static us.blockbox.biomefinder.BiomeFinder.*;

//Created 11/10/2016 1:48 AM
public class CommandBiomeReload implements CommandExecutor{

	private final JavaPlugin plugin;
	private final Logger log;

	public CommandBiomeReload(JavaPlugin plugin){
		this.plugin = plugin;
		log = plugin.getLogger();
	}

	@Override
	public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args){
		if(CacheBuilder.cacheBuildRunning){
			log.info("A cache is currently being built. Try again when it's finished.");
			return true;
		}

		saveBiomeCaches();
		loadBiomeCaches();
		loadConfig();
		sender.sendMessage(ChatColor.GREEN + prefix + "Config and caches reloaded.");
		return true;
	}
}
