package us.blockbox.biomefinder;

import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CacheManager{
	private Map<World,Map<Biome,Set<Coord>>> biomeCache;
	private final Map<World,Map<Biome,Set<Coord>>> biomeCacheOriginal;

	CacheManager(Map<World,Map<Biome,Set<Coord>>> biomeCache){
		this.biomeCache = new HashMap<>(biomeCache);
		this.biomeCacheOriginal = Collections.unmodifiableMap(new HashMap<>(biomeCache));
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

	/**
	 * @param w The World whose cache to check for changes
	 * @return True if the cache has not changed since it was loaded
	 * @deprecated This method hasn't been tested yet.
	 */
	@Deprecated
	public boolean isCacheUnchanged(World w){//todo test
		final Map<Biome,Set<Coord>> wCurrent = biomeCache.get(w);
		final Map<Biome,Set<Coord>> wOld = biomeCacheOriginal.get(w);
		return wCurrent.equals(wOld);
	}

	/**
	 * @return True if the cache has not changed since it was loaded
	 */
	public boolean isCacheUnchanged(){
		return biomeCache.equals(biomeCacheOriginal);
	}
}
