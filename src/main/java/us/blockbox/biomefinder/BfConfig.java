package us.blockbox.biomefinder;

import com.google.common.base.Charsets;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import us.blockbox.biomefinder.locale.BfLocale;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Logger;

public class BfConfig{
	private final BiomeFinder plugin;
	private final Logger log;
	private FileConfiguration config;
	private int points = 64;
	private int distance = 128;
	private int biomePointsMax = 50;
	private int nearbyRadius = 512;
	private boolean checkUpdate = false;
	private boolean logColorEnabled;
	private BfLocale bfLocale;
	private boolean versionChanged = false;

	BfConfig(BiomeFinder plugin){
		if(plugin == null){
			throw new IllegalArgumentException();
		}
		this.plugin = plugin;
		plugin.saveDefaultConfig();
		config = plugin.getConfig();
		log = plugin.getLogger();
	}

	public Map<World,Map<Biome,Set<Coord>>> loadBiomeCaches(){
		final Map<World,Map<Biome,Set<Coord>>> biomeCache = new HashMap<>();
		for(final World w : Bukkit.getServer().getWorlds()){
			final File cacheFile = new File(plugin.getDataFolder(),w.getName() + ".yml");
			if(!cacheFile.exists() || !cacheFile.isFile()){
				continue;
			}
			final FileConfiguration conf = YamlConfiguration.loadConfiguration(cacheFile);
			log.info("Loading biome cache for world " + w.getName());
//Biomes in world
			final Map<Biome,Set<Coord>> wCache = new EnumMap<>(Biome.class);
			for(final String biome : conf.getKeys(false)){
				if(biome.equals("points") || biome.equals("distance")){
					continue;
				}
				final List<String> stringList = conf.getStringList(biome);
				final Set<Coord> locs = new HashSet<>(stringList.size());
//Locations in biome
				for(final String location : stringList){
					final String[] i = location.split(",");
					locs.add(new Coord(Integer.valueOf(i[0]),Integer.valueOf(i[1])));
				}
				wCache.put(Biome.valueOf(biome),locs);
			}
			if(wCache.isEmpty()){
				continue;
			}
			biomeCache.put(w,wCache);
		}
		return biomeCache;
	}

	public void saveBiomeCaches(){
		final CacheManager cacheManager = plugin.getCacheManager();
		if(cacheManager.hasCacheChanged()){
			log.info("Cache hasn't changed, not resaving");
			return;
		}
		for(final World w : cacheManager.getCachedWorlds()){
			saveBiomeCache(w);
		}
	}

	void saveBiomeCache(World w){
		if(w == null || !plugin.getServer().getWorlds().contains(w)){
			return;
		}
		final CacheManager cacheManager = plugin.getCacheManager();
//		plugin.saveResource("blank.yml",true);
//		File blank = new File(plugin.getDataFolder(),"blank.yml");
//		FileConfiguration cacheFileNew = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(),"blank.yml"));
		final FileConfiguration cacheFileNew = new YamlConfiguration();
//		blank.delete();
		cacheFileNew.set("points",points);
		cacheFileNew.set("distance",distance);
//Biomes
		for(final Map.Entry<Biome,Set<Coord>> bLoc : cacheManager.getCache(w).entrySet()){
			final String b = bLoc.getKey().toString();
			final Set<Coord> value = bLoc.getValue();
			final List<String> locs = new ArrayList<>(value.size());
//Locations
			for(final Coord l : value){
				locs.add(l.x + "," + l.z);
			}
			cacheFileNew.set(b,locs);
		}
		try{
			cacheFileNew.save(new File(plugin.getDataFolder(),w.getName() + ".yml"));
		}catch(IOException e1){
			e1.printStackTrace();
		}
	}

	public void loadConfig(){
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
		config = plugin.getConfig();
		checkUpdate = config.getBoolean("checkupdate",true);

		loadLocale(new File(plugin.getDataFolder(),"locale.yml"));

		if(configNeedsUpdate(config.getInt("version",0))){
			versionChanged = true;
		}

		points = config.getInt("points",64);
		distance = config.getInt("distance",128);
		biomePointsMax = config.getInt("maxpoints",50);
		nearbyRadius = config.getInt("bsearchradius",512);
		logColorEnabled = config.getBoolean("colorlogs");

		if(points <= 0){
			points = 64;
		}
		if(distance <= 0){
			distance = 128;
		}

		if(distance % 16 != 0){
			log.warning("Distance is not a multiple of 16, defaulting to 128.");
			distance = 128;
		}
		if(biomePointsMax < 10){
			log.warning("Maximum points cannot be less than 10, defaulting to 50.");
			biomePointsMax = 50;
		}

		if(nearbyRadius < 128){
			log.warning("Nearby search radius must be at least 128, defaulting to 128.");
			nearbyRadius = 128;
		}
	}

	private boolean configNeedsUpdate(int version){
		return version < config.getDefaults().getInt("version");
	}

	private void loadLocale(File file){
		if(!file.exists() || !file.isFile()){
			plugin.saveResource(file.getName(),false);
		}
		if(bfLocale == null){
			bfLocale = new BfLocale(file.getName().replace(".yml",""));
		}
		final FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		final InputStream defConfigStream = plugin.getResource("locale.yml");
		config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream,Charsets.UTF_8)));
		bfLocale.loadLocale(config);
	}

	public BfLocale getLocale(){
		return bfLocale;
	}

	public int getPoints(){
		return points;
	}

	public int getDistance(){
		return distance;
	}

	public int getBiomePointsMax(){
		return biomePointsMax;
	}

	public int getNearbyRadius(){
		return nearbyRadius;
	}

	public boolean getCheckUpdate(){
		return checkUpdate;
	}

	public int getRecordedPoints(World w){
		Configuration c = getWorldConfig(w);
		if(c == null) return -1;
		return c.getInt("points",-1);
	}

	public int getRecordedDistance(World w){
		Configuration c = getWorldConfig(w);
		if(c == null) return -1;
		return c.getInt("points",-1);
	}

	private Configuration getWorldConfig(World w){
		if(w == null) return null;
		final File f = new File(plugin.getDataFolder(),w.getName() + ".yml");
		if(!f.isFile() || !f.exists()){
			return null;
		}
		return YamlConfiguration.loadConfiguration(f);
	}

	public boolean isLogColorEnabled(){
		return logColorEnabled;
	}

	public boolean isVersionChanged(){
		return versionChanged;
	}
}
