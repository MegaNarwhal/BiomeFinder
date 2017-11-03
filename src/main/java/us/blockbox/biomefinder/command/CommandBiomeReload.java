package us.blockbox.biomefinder.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import us.blockbox.biomefinder.BfConfig;
import us.blockbox.biomefinder.BiomeFinder;
import us.blockbox.biomefinder.CacheBuilder;
import us.blockbox.biomefinder.CacheManager;
import us.blockbox.biomefinder.locale.BfLocale;
import us.blockbox.biomefinder.locale.BfMessage;

public class CommandBiomeReload implements CommandExecutor{

	private final BfConfig bfc = BiomeFinder.getPlugin().getBfConfig();
	private final BfLocale locale = bfc.getLocale();
	private final CacheManager cacheManager = BiomeFinder.getPlugin().getCacheManager();

	public CommandBiomeReload(){
	}

	@Override
	public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args){
		if(!sender.hasPermission("biomefinder.reload")){
			sender.sendMessage(locale.getMessage(BfMessage.PLAYER_NO_PERMISSION));
			return true;
		}

		if(CacheBuilder.isBuildRunning()){
			sender.sendMessage("A cache is currently being built. Try again when it's finished.");
			return true;
		}

		bfc.saveBiomeCaches();
		cacheManager.setCache(bfc.loadBiomeCaches());
		bfc.loadConfig();
		final boolean stripColor = !bfc.isLogColorEnabled() && sender instanceof ConsoleCommandSender;
		String message = BfLocale.format(locale.getPrefix() + locale.getMessage(BfMessage.CONFIG_RELOADED),stripColor);
		sender.sendMessage(message);
		return true;
	}
}
