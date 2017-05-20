package us.blockbox.biomefinder;

import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CacheManager{
	private Map<World,Map<Biome,Set<Coord>>> biomeCache;
	private final Map<World,Map<Biome,Set<Coord>>> biomeCacheOriginal;

	CacheManager(Map<World,Map<Biome,Set<Coord>>> biomeCache){
		this.biomeCache = new HashMap<>(biomeCache);
		this.biomeCacheOriginal = new HashMap<>(biomeCache);
	}

	public void setCache(Map<World,Map<Biome,Set<Coord>>> biomeCache){
		this.biomeCache = biomeCache;
	}

	public Map<World,Map<Biome,Set<Coord>>> getCache(){
		return biomeCache;
	}

	public Map<Biome,Set<Coord>> getCache(World world){
		return biomeCache.get(world);
	}

	public Set<World> getCachedWorlds(){
		return biomeCache.keySet();
	}

	public boolean hasCache(World world){
		return biomeCache.containsKey(world);
	}

	boolean hasCacheChanged(){
		return biomeCache.equals(biomeCacheOriginal);
	}
}
