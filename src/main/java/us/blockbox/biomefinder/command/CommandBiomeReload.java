package us.blockbox.biomefinder.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import us.blockbox.biomefinder.BfConfig;
import us.blockbox.biomefinder.CacheBuilder;
import us.blockbox.biomefinder.api.CacheManager;
import us.blockbox.biomefinder.api.LocaleManager;
import us.blockbox.biomefinder.locale.BfLocale;
import us.blockbox.biomefinder.locale.BfMessage;

public class CommandBiomeReload implements CommandExecutor{
	private final BfConfig bfc;
	private final LocaleManager lm;
	private final CacheManager cacheManager;

	public CommandBiomeReload(BfConfig bfc,LocaleManager lm,CacheManager cacheManager){
		this.bfc = bfc;
		this.lm = lm;
		this.cacheManager = cacheManager;
	}

	@Override
	public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args){
		if(!sender.hasPermission("biomefinder.reload")){
			sender.sendMessage(lm.get(BfMessage.PLAYER_NO_PERMISSION));
			return true;
		}
		if(CacheBuilder.isBuildRunning()){
			sender.sendMessage("A cache is currently being built. Try again when it's finished.");
			return true;
		}
		bfc.saveBiomeCaches();
		cacheManager.setCache(bfc.loadBiomeCaches());
		bfc.loadConfig();
		lm.register(bfc.getLocale());
		final boolean stripColor = !bfc.isLogColorEnabled() && sender instanceof ConsoleCommandSender;
		String message = BfLocale.format(lm.getPrefix() + lm.get(BfMessage.CONFIG_RELOADED),stripColor);
		sender.sendMessage(message);
		return true;
	}
}
