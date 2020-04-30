package us.blockbox.biomefinder.api;

import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface TeleportManager{
	boolean tpToBiome(CommandSender sender,Player target,Biome b,LocationPreference pref);

	boolean tpToBiome(Player p,Biome b,LocationPreference preference);

	boolean tpToBiome(Player p,Biome b);

	public enum LocationPreference{
		NEAR,
		FAR,
		ANY
	}
}
