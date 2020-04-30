package us.blockbox.biomefinder;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import us.blockbox.biomefinder.api.CacheManager;
import us.blockbox.biomefinder.locale.BfLocale;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class BfConfig{
	private final BiomeFinder plugin;
	private final Logger log;
	private FileConfiguration config;

	//Defaults
	private static final int pointsDefault = 64;
	private static final int distanceDefault = 128;
	private static final int biomePointsMaxDefault = 50;
	private static final int nearbyRadiusDefault = 512;
	//End defaults

	private int points = pointsDefault;
	private int distance = distanceDefault;
	private int biomePointsMax = biomePointsMaxDefault;
	private int nearbyRadius = nearbyRadiusDefault;
	private boolean checkUpdate = false;
	private boolean logColorEnabled;
	private BfLocale bfLocale;
	private boolean versionChanged = false;

	BfConfig(BiomeFinder plugin){
		if(plugin == null){
			throw new IllegalArgumentException();
		}
		this.plugin = plugin;
		this.log = plugin.getLogger();
	}

	public Map<World,Map<Biome,Set<Coord>>> loadBiomeCaches(){
		final List<World> worlds = Bukkit.getServer().getWorlds();
		final Map<World,Map<Biome,Set<Coord>>> biomeCache = new HashMap<>(worlds.size());
		for(final World w : worlds){
			final File cacheFile = new File(plugin.getDataFolder(),w.getName() + ".yml");
			if(!cacheFile.exists() || !cacheFile.isFile()){
				continue;
			}
			final FileConfiguration conf = YamlConfiguration.loadConfiguration(cacheFile);
			final Map<Biome,Set<Coord>> wCache = loadBiomeCache(w,conf);
			if(wCache.isEmpty()){
				continue;
			}
			biomeCache.put(w,wCache);
		}
		return biomeCache;
	}

	private Map<Biome,Set<Coord>> loadBiomeCache(World w,FileConfiguration conf){
		Pattern comma = Pattern.compile(",");
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
				final String[] xz = comma.split(location);
				locs.add(new Coord(Integer.parseInt(xz[0]),Integer.parseInt(xz[1])));
			}
			wCache.put(Biome.valueOf(biome),locs);
		}
		return wCache;
	}

	public void saveBiomeCaches(){
		final CacheManager cacheManager = plugin.getCacheManager();
		if(cacheManager == null){
			throw new IllegalStateException("Cache manager is null, this should never happen!");
		}
		if(cacheManager.isCacheUnchanged()){
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
//		FileConfiguration confNew = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(),"blank.yml"));
		final FileConfiguration confNew = new YamlConfiguration();
//		blank.delete();
		confNew.set("points",points);
		confNew.set("distance",distance);
//Biomes
		List<Map.Entry<Biome,Set<Coord>>> entries = new ArrayList<>(cacheManager.getCache(w).entrySet());
		Collections.sort(entries,new Comparator<Map.Entry<Biome,Set<Coord>>>(){//todo test
			@Override
			public int compare(Map.Entry<Biome,Set<Coord>> o1,Map.Entry<Biome,Set<Coord>> o2){
				return o1.getKey().name().compareTo(o2.getKey().name());
			}
		});
		for(final Map.Entry<Biome,Set<Coord>> bLoc : entries){
			final String b = bLoc.getKey().toString();
			final Set<Coord> value = bLoc.getValue();
			final List<String> locs = new ArrayList<>(value.size());
//Locations
			for(final Coord l : value){
				locs.add(l.x + "," + l.z);
			}
			confNew.set(b,locs);
		}
		final File file = new File(plugin.getDataFolder(),w.getName() + ".yml");
		try{
			confNew.save(file);
		}catch(IOException e1){
			e1.printStackTrace();
		}
	}

	public void loadConfig(){
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
		config = plugin.getConfig();
		checkUpdate = config.getBoolean("checkupdate",true);

		try{
			this.bfLocale = BfLocale.create(plugin,plugin.prefix,null,new File(plugin.getDataFolder(),"locale.yml"));
			log.info("Loaded locale \"" + this.bfLocale.getLocaleName() + "\".");
		}catch(Exception e){
			e.printStackTrace();
		}

		if(configNeedsUpdate(config.getInt("version",0))){
			versionChanged = true;
		}

		points = config.getInt("points",pointsDefault);
		distance = config.getInt("distance",distanceDefault);
		biomePointsMax = config.getInt("maxpoints",biomePointsMaxDefault);
		nearbyRadius = config.getInt("bsearchradius",nearbyRadiusDefault);
		logColorEnabled = config.getBoolean("colorlogs");

		if(points <= 0){
			points = pointsDefault;
		}
		if(distance <= 0){
			distance = distanceDefault;
		}

		if(distance % 16 != 0){
			log.warning("Distance is not a multiple of 16, defaulting to " + distanceDefault + '.');
			distance = distanceDefault;
		}
		if(biomePointsMax < 10){
			log.warning("Maximum points cannot be less than 10, defaulting to " + biomePointsMaxDefault + '.');
			biomePointsMax = biomePointsMaxDefault;
		}

		if(nearbyRadius < 128){
			log.warning("Nearby search radius must be at least 128, defaulting to " + nearbyRadiusDefault + '.');
			nearbyRadius = nearbyRadiusDefault;
		}
	}

	private boolean configNeedsUpdate(int version){
		return version < config.getDefaults().getInt("version");
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
		return c.getInt("distance",-1);
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
