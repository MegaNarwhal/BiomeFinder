package us.blockbox.biomefinder.command;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.blockbox.biomefinder.BfConfig;
import us.blockbox.biomefinder.api.CacheManager;
import us.blockbox.biomefinder.api.TeleportManager;
import us.blockbox.biomefinder.api.TeleportManager.LocationPreference;
import us.blockbox.biomefinder.locale.BfLocale;
import us.blockbox.biomefinder.locale.BfMessage;
import us.blockbox.biomefinder.util.Biomes;

import java.util.Locale;

public class CommandBfTp implements CommandExecutor{
	private final BfConfig bfc;
	private final BfLocale locale;
	private final CacheManager cacheManager;
	private final TeleportManager tpManager;

	public CommandBfTp(BfConfig config,CacheManager cacheManager,TeleportManager tpManager){
		this.bfc = config;
		this.tpManager = tpManager;
		this.locale = bfc.getLocale();
		this.cacheManager = cacheManager;
	}

	@Override
	public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args){
		if(!sender.hasPermission("biomefinder.tp")){
			sender.sendMessage(locale.getPrefix() + locale.getMessage(BfMessage.PLAYER_NO_PERMISSION));
			return true;
		}
		final Player target = getTarget(sender,args);
		if(target == null){
			//any error message was sent in getTarget, just return
			return true;
		}
		if(sender != target && !sender.hasPermission("biomefinder.tp.other")){
			sender.sendMessage(locale.getMessage(BfMessage.PLAYER_NO_PERMISSION));
			return true;
		}
		final World world = target.getWorld();
		if(!cacheManager.hasCache(world)){
			sender.sendMessage(locale.getPrefix() + locale.getMessage(BfMessage.WORLD_INDEX_MISSING));
			return true;
		}
		if(args.length < 1){
			//don't need to consider if the sender isn't the target here
			//you need at least 2 args to have a sender other than yourself
			sender.sendMessage(locale.getPrefix() + locale.getMessage(BfMessage.BIOME_NAME_UNSPECIFIED));
			return true;
		}
		final Biome b = Biomes.matchPartial(args[0]);
		if(b == null){
			sender.sendMessage(locale.getPrefix() + locale.getMessage(BfMessage.BIOME_NAME_UNSPECIFIED));
		}else{
			final LocationPreference pref = getLocationPreference(args);
			tpManager.tpToBiome(sender,target,b,pref);
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	private Player getTarget(CommandSender sender,String[] args){
		Player target = null;
		int argsLength = args.length;
		if(argsLength >= 2){
			//we have biome name and player name at least
			String name = args[argsLength - 1];//last argument should be player name
			Player byName = Bukkit.getPlayer(name);
			if(byName == null){
				if(argsLength > 2){
					//we know there was a keyword so the last arg is definitely the player name
					//player wasn't found by name so return
					sender.sendMessage(BfLocale.format(locale.getPrefix() + String.format(locale.getMessage(BfMessage.PLAYER_NOT_FOUND),name),!bfc.isLogColorEnabled()));
					return null;
				}
				//keep going with the assumption that the last arg is a keyword, not a player name
			}else{
				target = byName;
			}
		}
		if(target == null){
			if(sender instanceof Player){
				target = ((Player)sender);
			}else{
				sender.sendMessage(BfLocale.format(locale.getPrefix() + locale.getMessage(BfMessage.COMMAND_NOT_PLAYER),!bfc.isLogColorEnabled()));
				return null;
			}
		}
		return target;
	}

	private static LocationPreference getLocationPreference(String[] args){
		if(args.length >= 2){
			final String distanceArg = args[1].toLowerCase(Locale.US);
			if(distanceArg.startsWith("near")){
				return LocationPreference.NEAR;
			}else if(distanceArg.startsWith("far")){
				return LocationPreference.FAR;
			}
		}
		return LocationPreference.ANY;
	}
}
