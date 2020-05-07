package us.blockbox.biomefinder.locale;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class BfLocale{
	private static final Pattern UNDERSCORES = Pattern.compile("_");
	private final String localeName;
	private final ImmutableMap<BfMessage,String> messages;

	public String getLocaleName(){
		return localeName;
	}

	private BfLocale(String localeName,ImmutableMap<BfMessage,String> messages){
		this.localeName = Objects.requireNonNull(localeName);
		this.messages = Objects.requireNonNull(messages);
	}

	public String getPrefix(){
		return getMessage(BfMessage.PLUGIN_PREFIX);
	}

	public static BfLocale create(Plugin plugin,String localeName,File file) throws IOException, IllegalArgumentException{
		if(!file.exists() || !file.isFile()){
			plugin.saveResource(file.getName(),false);
		}
		final FileConfiguration config = getConfigWithDefaults(plugin,file);
		String name = localeName == null ? file.getName().replace(".yml","") : localeName;
		ImmutableMap<BfMessage,String> messageMap = buildMap(config);
		checkNewOptions(plugin.getLogger(),config);
		return new BfLocale(name,messageMap);
	}

	private static FileConfiguration getConfigWithDefaults(Plugin plugin,File file){
		final FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		final InputStream defConfigStream = plugin.getResource("locale.yml");
		config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream,Charsets.UTF_8)));
		return config;
	}

	private static ImmutableMap<BfMessage,String> buildMap(FileConfiguration config){
		Map<BfMessage,String> messages = new EnumMap<>(BfMessage.class);
		for(BfMessage m : BfMessage.values()){
			final String text = config.getString(m.name());
			messages.put(m,ChatColor.translateAlternateColorCodes('&',text));
		}
		//noinspection UnstableApiUsage
		return Maps.immutableEnumMap(messages);
	}

	private static void checkNewOptions(Logger log,FileConfiguration config){
		Set<String> userSet = config.getKeys(true);
		Set<String> defaults = config.getDefaults().getKeys(true);
		Sets.SetView<String> newOptions = Sets.difference(defaults,userSet);
		if(!newOptions.isEmpty()){
			log.warning("New messages have been added to the locale:");
			log.warning(newOptions.toString());
			log.warning("Please delete or move your currently locale file to be able to customize these new options.");
		}
	}

	public String getFriendlyName(Biome biome){
		return WordUtils.capitalizeFully(UNDERSCORES.matcher(biome.name()).replaceAll(" "));
	}

	public static String format(String message,boolean stripColor){//todo move
		if(stripColor){
			return ChatColor.stripColor(message);
		}
		return message;
	}

	public String getMessage(BfMessage msg){
		return messages.get(msg);
	}
}
