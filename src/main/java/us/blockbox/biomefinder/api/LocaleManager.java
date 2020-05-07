package us.blockbox.biomefinder.api;

import org.bukkit.block.Biome;
import us.blockbox.biomefinder.locale.BfLocale;
import us.blockbox.biomefinder.locale.BfMessage;

public interface LocaleManager{
	String getPrefix();

	String getPrefix(String localeName);

	String get(BfMessage msg);

	String get(String localeName,BfMessage msg);

	BfLocale register(BfLocale locale);

	String getBiome(Biome biome);

	String getBiome(String localeName,Biome biome);
}
