package us.blockbox.biomefinder;

import com.google.common.base.Charsets;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import us.blockbox.biomefinder.locale.BfLocale;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Logger;

import static us.blockbox.biomefinder.ConsoleMessager.warn;
import static us.blockbox.biomefinder.BiomeFinder.biomeCache;
import static us.blockbox.biomefinder.BiomeFinder.biomeCacheOriginal;
import static us.blockbox.biomefinder.BiomeFinder.plugin;

//Created 11/10/2016 1:55 AM
public class BfConfig{

	private static BfConfig ourInstance = new BfConfig();

	static BfConfig getInstance(){
		return ourInstance;
	}

	private BfConfig(){
	}

	//private static Map<World,FileConfiguration> cacheConfigurations = new HashMap<>();
	private static final Logger log = Bukkit.getLogger();
	private static FileConfiguration config;
	private static int points = 64;
	private static int distance = 128;
	private static int biomePointsMax = 50;
	private static int nearbyRadius = 512;
	private static boolean checkUpdate = false;
	private static BfLocale bfLocale;

	static{
		plugin.saveDefaultConfig();
		config = plugin.getConfig();
		checkUpdate = config.getBoolean("checkupdate",true);
	}

	public static void loadBiomeCaches(){
		biomeCache.clear();
//		cacheConfigurations.clear();
		for(final World w : Bukkit.getServer().getWorlds()){
			final File cacheFile = new File(plugin.getDataFolder(),w.getName() + ".yml");
			if(!cacheFile.exists() || !cacheFile.isFile()){
				continue;
			}
			FileConfiguration conf = YamlConfiguration.loadConfiguration(cacheFile);
			log.info("Loading biome cache for world " + w.getName());
//Biomes in world
			Map<Biome,Set<Coord>> wCache = new HashMap<>();
			for(String biome : conf.getKeys(false)){
				if(biome.equals("points") || biome.equals("distance")){
					continue;
				}
				Set<Coord> locs = new HashSet<>();
//Locations in biome
				for(String location : conf.getStringList(biome)){
					String[] i = location.split(",");
					locs.add(new Coord(Integer.valueOf(i[0]),Integer.valueOf(i[1])));
				}
				wCache.put(Biome.valueOf(biome),locs);
			}
			if(wCache.isEmpty()){
				continue;
			}
			biomeCache.put(w,wCache);
			biomeCacheOriginal = new HashMap<>(biomeCache);
		}
	}

	public static void saveBiomeCaches(){
		if(biomeCache.equals(biomeCacheOriginal)){
			log.info("Cache hasn't changed, not resaving");
			return;
		}
		for(Map.Entry<World,Map<Biome,Set<Coord>>> e : biomeCache.entrySet()){
			saveBiomeCache(e.getKey());
		}
	}

	static void saveBiomeCache(World w){
		if(!plugin.getServer().getWorlds().contains(w) || w == null){
			return;
		}

		plugin.saveResource("blank.yml",true);
		File blank = new File(plugin.getDataFolder(),"blank.yml");
		FileConfiguration cacheFileNew = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(),"blank.yml"));
		blank.delete();

		cacheFileNew.set("points",BfConfig.getPoints());
		cacheFileNew.set("distance",BfConfig.getDistance());

//Biomes
		for(Map.Entry<Biome,Set<Coord>> bLoc : biomeCache.get(w).entrySet()){
			String b = bLoc.getKey().toString();
			List<String> locs = new ArrayList<>();

//Locations
			for(Coord l : bLoc.getValue()){
				locs.add(l.x + "," + l.z);
			}
			cacheFileNew.set(b,locs);
		}

		try{
			cacheFileNew.save(new File(plugin.getDataFolder(),w.getName() + ".yml"));
		}catch(IOException e1){
			e1.printStackTrace();
		}

//		cacheConfigurations.put(w,cacheFileNew);
	}

	private static boolean configNeedsUpdate(int version){
		return version < 2;
	}

	public static void loadConfig(){
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
		config = plugin.getConfig();
		loadLocale(new File(plugin.getDataFolder(),"locale.yml"));

		if(configNeedsUpdate(config.getInt("version",0))){
			warn("The config format has been changed. New options may have been added or new defaults set. Please regenerate your config to take advantage of any changes.");
		}

		points = config.getInt("points",64);
		distance = config.getInt("distance",128);
		biomePointsMax = config.getInt("maxpoints",50);
		nearbyRadius = config.getInt("bsearchradius",512);

		if(points <= 0){
			points = 64;
		}
		if(distance <= 0){
			distance = 128;
		}

		if(distance %16 != 0){
			log.info("Distance is not a multiple of 16, defaulting to 128.");
			distance = 128;
		}
		if(biomePointsMax < 10){
			log.info("Maximum points cannot be less than 10, defaulting to 50.");
			biomePointsMax = 50;
		}

		if(nearbyRadius < 128){
			log.info("Nearby search radius must be at least 128, defaulting to 128.");
			nearbyRadius = 128;
		}
	}

	private static void loadLocale(File file){
		if(!file.exists() || !file.isFile()){
			plugin.saveResource(file.getName(),false);
		}
		if(bfLocale == null){
			bfLocale = new BfLocale(file.getName().replace(".yml",""));
		}
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		InputStream defConfigStream = plugin.getResource("locale.yml");
		config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
		bfLocale.loadLocale(config);
	}

	public static BfLocale getLocale(){
		return bfLocale;
	}

	public static int getPoints(){
		return points;
	}

	public static int getDistance(){
		return distance;
	}

	public static int getBiomePointsMax(){
		return biomePointsMax;
	}

	public static int getNearbyRadius(){
		return nearbyRadius;
	}

	public static boolean getCheckUpdate(){
		return checkUpdate;
	}

	public static int getRecordedPoints(World w){
		if(w == null) return -1;
		File f = new File(plugin.getDataFolder(),w.getName() + ".yml");
		if(!f.isFile() || !f.exists()){
			return -1;
		}
		FileConfiguration c = YamlConfiguration.loadConfiguration(f);
		return c.getInt("points",-1);
	}
}
