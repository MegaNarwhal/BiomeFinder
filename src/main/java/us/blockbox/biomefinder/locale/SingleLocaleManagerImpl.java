package us.blockbox.biomefinder.locale;

import org.bukkit.block.Biome;
import us.blockbox.biomefinder.api.LocaleManager;

/**
 * A LocaleManager that contains only one BfLocale. Registering multiple locales leads to the most recently registered
 * instance being used. Methods that take locale names simply ignore the supplied name.
 */
public final class SingleLocaleManagerImpl implements LocaleManager{
	private BfLocale locale;

	@Override
	public String getPrefix(){
		return locale.getPrefix();
	}

	@Override
	public String getPrefix(String localeName){
		return locale.getPrefix();
	}

	@Override
	public String get(BfMessage msg){
		return locale.getMessage(msg);
	}

	@Override
	public String get(String localeName,BfMessage msg){
		return locale.getMessage(msg);
	}

	@Override
	public BfLocale register(BfLocale locale){
		BfLocale localeOld = this.locale;
		this.locale = locale;
		return localeOld;
	}

	@Override
	public String getBiome(Biome biome){
		return locale.getFriendlyName(biome);
	}

	@Override
	public String getBiome(String localeName,Biome biome){
		return locale.getFriendlyName(biome);
	}
}
