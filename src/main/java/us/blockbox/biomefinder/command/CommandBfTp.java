package us.blockbox.biomefinder.command;

import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.blockbox.biomefinder.BfConfig;
import us.blockbox.biomefinder.BiomeFinder;
import us.blockbox.biomefinder.locale.BfLocale;
import us.blockbox.biomefinder.locale.BfMessage;

import static us.blockbox.biomefinder.BiomeFinder.*;

public class CommandBfTp implements CommandExecutor{

	private final BfConfig bfc = BiomeFinder.getPlugin().getBfConfig();
	private final BfLocale locale = bfc.getLocale();

	@Override
	public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args){
		if(!(sender instanceof Player)){
			sender.sendMessage(BfLocale.format(prefix + locale.getMessage(BfMessage.COMMAND_NOT_PLAYER),!bfc.isLogColorEnabled()));
			return true;
		}
		if(!sender.hasPermission("biomefinder.tp")){
			sender.sendMessage(prefix + locale.getMessage(BfMessage.PLAYER_NO_PERMISSION));
			return true;
		}
		final Player p = (Player)sender;
		if(!hasCache(p.getWorld())){
			sender.sendMessage(prefix + locale.getMessage(BfMessage.WORLD_INDEX_MISSING));
			return true;
		}
		if(args.length < 1){
			sender.sendMessage(prefix + locale.getMessage(BfMessage.BIOME_NAME_UNSPECIFIED));
			return true;
		}
		Biome b = parseBiome(args[0]);
		if(b == null){
			sender.sendMessage(prefix + locale.getMessage(BfMessage.BIOME_NAME_UNSPECIFIED));
			return true;
		}
		if(args.length >= 2){
			if(args[1].toLowerCase().startsWith("near")){
				tpToBiome(p,b,true);
				return true;
			}
		}
		tpToBiome(p,b);
		return true;
	}
}