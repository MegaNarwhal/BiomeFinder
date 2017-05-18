package us.blockbox.biomefinder.locale;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

public class BfLocale{

	private final String locale;
	private static final Pattern underscores = Pattern.compile("_");
	private final Map<BfMessage,String> messages = new EnumMap<>(BfMessage.class);
	private final EnumMap<Biome,String> friendlyNames = new EnumMap<>(Biome.class);

	public BfLocale(String locale){
		this.locale = locale;
	}

	public void loadLocale(FileConfiguration config){
		messages.clear();
		for(BfMessage m : BfMessage.values()){
			messages.put(m,ChatColor.translateAlternateColorCodes('&',config.getString(m.toString())));
		}
	}

	public String getFriendlyName(Biome biome){
		return WordUtils.capitalizeFully(underscores.matcher(biome.name()).replaceAll(" "));
	}

	public static String format(String message,boolean stripColor){
		if(stripColor){
			return ChatColor.stripColor(message);
		}
		return message;
	}

	public String getMessage(BfMessage msg){
		return messages.get(msg);
	}
}
