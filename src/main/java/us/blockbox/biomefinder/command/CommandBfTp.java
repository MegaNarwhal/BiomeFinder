package us.blockbox.biomefinder.command;

import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.blockbox.biomefinder.BfConfig;
import us.blockbox.biomefinder.locale.BfLocale;
import us.blockbox.biomefinder.locale.BfMessage;

import static us.blockbox.biomefinder.BiomeFinder.*;

//Created 11/10/2016 1:44 AM
public class CommandBfTp implements CommandExecutor{

	private BfLocale locale = BfConfig.getLocale();

	@Override
	public boolean onCommand(CommandSender sender,Command cmd,String label,String[] args){
		if(!(sender instanceof Player)){
			sender.sendMessage(prefix + "You must be a player to use this command.");
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