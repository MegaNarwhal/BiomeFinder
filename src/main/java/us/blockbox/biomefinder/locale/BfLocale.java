package us.blockbox.biomefinder.locale;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

//Created 11/13/2016 2:08 AM
public class BfLocale{

	String locale;
	File file;
	private Map<BfMessage,String> messages = new HashMap<>();

	public BfLocale(String locale,File config){
		this.locale = locale;
		this.file = config;
		loadLocale();
	}

	public void loadLocale(){
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		for(BfMessage m : BfMessage.values()){
			messages.put(m,ChatColor.translateAlternateColorCodes('&',config.getString(m.toString())));
		}
	}

	public String getMessage(BfMessage msg){
		return messages.get(msg);
	}
}
