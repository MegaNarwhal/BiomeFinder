package us.blockbox.biomefinder.locale;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.EnumMap;
import java.util.Map;

public class BfLocale{

	private final String locale;
	private final Map<BfMessage,String> messages = new EnumMap<>(BfMessage.class);

	public BfLocale(String locale){
		this.locale = locale;
	}

	public void loadLocale(FileConfiguration config){
		messages.clear();
		for(BfMessage m : BfMessage.values()){
			messages.put(m,ChatColor.translateAlternateColorCodes('&',config.getString(m.toString())));
		}
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
