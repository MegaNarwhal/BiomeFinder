package us.blockbox.biomefinder.locale;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

//Created 11/13/2016 2:08 AM
public class BfLocale{

	final String locale;
	private final Map<BfMessage,String> messages = new HashMap<>();

	public BfLocale(String locale){
		this.locale = locale;
	}

	public void loadLocale(FileConfiguration config){
		messages.clear();
		for(BfMessage m : BfMessage.values()){
			messages.put(m,ChatColor.translateAlternateColorCodes('&',config.getString(m.toString())));
		}
	}

	public String getMessage(BfMessage msg){
		return messages.get(msg);
	}
}
